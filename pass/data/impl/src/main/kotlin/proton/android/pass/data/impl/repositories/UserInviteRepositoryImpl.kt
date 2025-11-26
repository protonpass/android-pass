/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.repositories.UserInviteRepository
import proton.android.pass.data.api.usecases.ObserveConfirmedInviteToken
import proton.android.pass.data.impl.crypto.EncryptUserInviteKeys
import proton.android.pass.data.impl.crypto.ReencryptUserInviteContents
import proton.android.pass.data.impl.db.entities.UserInviteEntity
import proton.android.pass.data.impl.db.entities.UserInviteKeyEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalUserInviteDataSource
import proton.android.pass.data.impl.local.UserInviteAndKeysEntity
import proton.android.pass.data.impl.remote.RemoteUserInviteDataSource
import proton.android.pass.data.impl.requests.invites.AcceptInviteRequest
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.data.impl.responses.PendingUserInviteResponse
import proton.android.pass.data.impl.responses.SuggestedEmail
import proton.android.pass.domain.Group
import proton.android.pass.domain.GroupMember
import proton.android.pass.domain.InviteRecommendations
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.RecommendedEmail
import proton.android.pass.domain.RecommendedGroup
import proton.android.pass.domain.RecommendedItem
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareInvite
import proton.android.pass.domain.ShareType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class UserInviteRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteUserInviteDataSource,
    private val localDatasource: LocalUserInviteDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val reencryptUserInviteContents: ReencryptUserInviteContents,
    private val encryptUserInviteKeys: EncryptUserInviteKeys,
    private val observeConfirmedInviteToken: ObserveConfirmedInviteToken,
    private val groupRepository: GroupRepository,
    private val appDispatchers: AppDispatchers,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : UserInviteRepository {

    override suspend fun getInvite(userId: UserId, inviteToken: InviteToken): Option<PendingInvite> =
        localDatasource.getInvite(userId, inviteToken)
            .let { inviteEntityOption ->
                when (inviteEntityOption) {
                    None -> None
                    is Some -> encryptionContextProvider.withEncryptionContextSuspendable {
                        inviteEntityOption.value
                            .toDomain(this)
                            .some()
                    }
                }
            }

    override fun observeInvites(userId: UserId): Flow<List<PendingInvite>> = localDatasource
        .observeAllInvites(userId)
        .map { entities ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                entities.map { it.toDomain(this) }
            }
        }

    override suspend fun refreshInvites(userId: UserId): Boolean = coroutineScope {
        PassLogger.i(TAG, "Refresh invites started")
        val deferredRemoteInvites: Deferred<List<PendingUserInviteResponse>> =
            async { remoteDataSource.fetchInvites(userId) }
        deferredRemoteInvites.invokeOnCompletion {
            if (it != null) {
                PassLogger.w(TAG, it)
            } else {
                PassLogger.i(TAG, "Fetched remote invites")
            }
        }
        val deferredLocalInvites: Deferred<List<UserInviteEntity>> =
            async { localDatasource.observeAllInvites(userId).first() }
        deferredLocalInvites.invokeOnCompletion {
            if (it != null) {
                PassLogger.w(TAG, it)
            } else {
                PassLogger.i(TAG, "Retrieved local invites")
            }
        }

        val remoteInvites = deferredRemoteInvites.await()
        PassLogger.i(TAG, "Fetched ${remoteInvites.size} remote invites")

        val localInvites = deferredLocalInvites.await()
        PassLogger.i(TAG, "Retrieved ${localInvites.size} local invites")

        // Remove deleted invites
        val deletedInvites = localInvites.filter { local ->
            remoteInvites.none { remote -> remote.inviteToken == local.token }
        }
        if (deletedInvites.isNotEmpty()) {
            PassLogger.i(TAG, "Deleting ${deletedInvites.size} invites")
            localDatasource.removeInvites(deletedInvites)
        }

        // Insert new invites
        val newInvites = remoteInvites.filter { remote ->
            localInvites.none { local -> local.token == remote.inviteToken }
        }
        val hasNewInvites = newInvites.isNotEmpty()

        // Detect if we have a new confirmed invite
        newInvites.firstOrNull { newInvite -> newInvite.fromNewUser }
            ?.let { newUserInvite -> InviteToken(newUserInvite.inviteToken) }
            ?.also { newUserInviteToken -> observeConfirmedInviteToken.send(inviteToken = newUserInviteToken) }

        val invitesWithKeys: List<UserInviteAndKeysEntity> = newInvites
            .map { invite -> inviteAndKeysEntity(invite, userId) }

        if (invitesWithKeys.isNotEmpty()) {
            PassLogger.i(TAG, "Inserting ${invitesWithKeys.size} invites")
            localDatasource.storeInvites(invitesWithKeys)
        }
        hasNewInvites
    }

    private suspend fun inviteAndKeysEntity(
        invite: PendingUserInviteResponse,
        userId: UserId
    ): UserInviteAndKeysEntity {
        val vaultData = invite.vaultData
        val reencryptedInviteContent = reencryptUserInviteContents(userId, invite)
        val userInviteEntity = UserInviteEntity(
            token = invite.inviteToken,
            userId = userId.id,
            inviterEmail = invite.inviterEmail,
            invitedEmail = invite.invitedEmail,
            invitedAddressId = invite.invitedAddressId,
            memberCount = vaultData?.memberCount ?: 0,
            itemCount = vaultData?.itemCount ?: 0,
            reminderCount = invite.remindersSent,
            shareContent = vaultData?.content.orEmpty(),
            shareContentKeyRotation = vaultData?.contentKeyRotation ?: -1L,
            shareContentFormatVersion = vaultData?.contentFormatVersion ?: -1,
            createTime = invite.createTime,
            encryptedContent = reencryptedInviteContent.encryptedContent,
            fromNewUser = invite.fromNewUser,
            shareType = invite.targetType
        )

        val inviteKeys = invite.keys.map { key ->
            UserInviteKeyEntity(
                inviteToken = invite.inviteToken,
                key = key.key,
                keyRotation = key.keyRotation,
                createTime = invite.createTime
            )
        }

        return UserInviteAndKeysEntity(
            userInviteEntity = userInviteEntity,
            inviteKeys = inviteKeys
        )
    }

    override suspend fun acceptInvite(userId: UserId, inviteToken: InviteToken): ShareInvite {
        val invite = localDatasource.getInviteWithKeys(userId, inviteToken).value()
            ?: throw IllegalStateException("Could not find the invite: ${inviteToken.value}")

        val keys: List<InviteKeyRotation> = encryptUserInviteKeys(
            userId = userId,
            invite = invite
        )
        val request = AcceptInviteRequest(keys)
        val responseShare = remoteDataSource.acceptInvite(userId, inviteToken, request)
        localDatasource.removeInvite(userId, inviteToken)

        return ShareInvite(
            shareId = ShareId(responseShare.shareId),
            itemId = ItemId(responseShare.targetId)
        )
    }

    override suspend fun rejectInvite(userId: UserId, inviteToken: InviteToken) {
        remoteDataSource.rejectInvite(userId, inviteToken)
        localDatasource.removeInvite(userId, inviteToken)
    }

    override fun observeInviteRecommendations(
        userId: UserId,
        shareId: ShareId,
        lastToken: String?,
        startsWith: String?
    ): Flow<InviteRecommendations> = flow {
        val suggestedResponse = remoteDataSource.fetchInviteRecommendationsSuggested(
            userId = userId,
            shareId = shareId,
            startsWith = startsWith
        )

        val orgState = observeOrgRecommendationsRecursive(
            userId = userId,
            shareId = shareId,
            since = lastToken,
            startsWith = startsWith,
            currentState = OrgState(
                cumulativePlanRecommendedEmails = emptySet(),
                groupDisplayName = null
            )
        )

        val isGroupsEnabled = featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_GROUP_SHARE)
            .firstOrNull()
            ?: false
        val groupData = if (isGroupsEnabled) fetchGroupData(userId) else GroupData()
        val recommendedItems = processSuggestedItems(suggestedResponse.suggested, groupData)

        val organizationItems = processOrganizationItems(
            emails = orgState.cumulativePlanRecommendedEmails,
            groupData = groupData
        )
        val allGroupsAsRecommendedItems = createAllGroupsAsRecommendedItems(
            groups = groupData.groups,
            membersMap = groupData.membersMap
        )

        emit(
            InviteRecommendations(
                recommendedItems = recommendedItems,
                groupDisplayName = orgState.groupDisplayName.orEmpty(),
                organizationItems = (allGroupsAsRecommendedItems + organizationItems).distinctBy { it.email }
            )
        )
    }.flowOn(appDispatchers.io)

    private suspend fun observeOrgRecommendationsRecursive(
        userId: UserId,
        shareId: ShareId,
        since: String?,
        startsWith: String?,
        currentState: OrgState
    ): OrgState {
        val result = remoteDataSource.fetchInviteRecommendationsOrganization(
            userId = userId,
            shareId = shareId,
            since = since,
            startsWith = startsWith
        )

        val recommendation = result.recommendation
        val updatedState = currentState.copy(
            cumulativePlanRecommendedEmails = currentState.cumulativePlanRecommendedEmails +
                recommendation.entries.map { it.email },
            groupDisplayName = recommendation.groupDisplayName ?: currentState.groupDisplayName
        )

        if (recommendation.nextToken.isNullOrBlank() || recommendation.entries.isEmpty()) {
            return updatedState
        }

        return observeOrgRecommendationsRecursive(
            userId = userId,
            shareId = shareId,
            since = recommendation.nextToken,
            startsWith = startsWith,
            currentState = updatedState
        )
    }

    private data class GroupData(
        val groups: List<Group> = emptyList(),
        val groupMapByEmail: Map<String, Group> = emptyMap(),
        val membersMap: Map<Group, List<GroupMember>> = emptyMap()
    )

    private suspend fun fetchGroupData(userId: UserId): GroupData {
        val groups = groupRepository.retrieveGroups(userId, forceRefresh = false)
        val groupMapByEmail = groups
            .mapNotNull { group ->
                group.groupEmail?.takeIf { it.isNotBlank() }?.let { email ->
                    email to group
                }
            }
            .toMap()
        val membersMap = groups.associateWith { group ->
            groupRepository.retrieveGroupMembers(userId, group.id, forceRefresh = false)
        }
        return GroupData(groups, groupMapByEmail, membersMap)
    }

    private fun processSuggestedItems(suggested: List<SuggestedEmail>, groupData: GroupData): List<RecommendedItem> =
        suggested.map { suggestedItem ->
            if (suggestedItem.isGroup) {
                val group = groupData.groupMapByEmail[suggestedItem.email]
                if (group != null) {
                    val members = groupData.membersMap[group].orEmpty()
                    RecommendedGroup(
                        groupId = group.id,
                        email = suggestedItem.email,
                        name = group.name,
                        memberCount = members.size
                    )
                } else {
                    RecommendedEmail(suggestedItem.email)
                }
            } else {
                RecommendedEmail(suggestedItem.email)
            }
        }

    private fun processOrganizationItems(emails: Set<String>, groupData: GroupData): List<RecommendedItem> =
        emails.map { email ->
            val group = groupData.groupMapByEmail[email]
            if (group != null) {
                val members = groupData.membersMap[group].orEmpty()
                RecommendedGroup(
                    groupId = group.id,
                    email = email,
                    name = group.name,
                    memberCount = members.size
                )
            } else {
                RecommendedEmail(email)
            }
        }

    private fun createAllGroupsAsRecommendedItems(
        groups: List<Group>,
        membersMap: Map<Group, List<GroupMember>>
    ): List<RecommendedGroup> = groups.mapNotNull { group ->
        group.groupEmail?.takeIf { it.isNotBlank() }?.let { email ->
            val members = membersMap[group] ?: emptyList()
            RecommendedGroup(
                groupId = group.id,
                email = email,
                name = group.name,
                memberCount = members.size
            )
        }
    }

    private data class OrgState(
        val cumulativePlanRecommendedEmails: Set<String>,
        val groupDisplayName: String?
    )

    private fun UserInviteEntity.toDomain(encryptionContext: EncryptionContext): PendingInvite =
        when (ShareType.from(shareType)) {
            ShareType.Item -> {
                PendingInvite.UserItem(
                    inviteToken = InviteToken(token),
                    inviterEmail = inviterEmail,
                    invitedAddressId = invitedAddressId,
                    isFromNewUser = fromNewUser
                )
            }

            ShareType.Vault -> {
                val content = encryptionContext.decrypt(encryptedContent)
                val decoded = VaultV1.Vault.parseFrom(content)

                PendingInvite.UserVault(
                    inviteToken = InviteToken(token),
                    inviterEmail = inviterEmail,
                    invitedAddressId = invitedAddressId,
                    memberCount = memberCount,
                    itemCount = itemCount,
                    name = decoded.name,
                    icon = decoded.display.icon.toDomain(),
                    color = decoded.display.color.toDomain(),
                    isFromNewUser = fromNewUser
                )
            }
        }


    private companion object {

        private const val TAG = "InviteRepositoryImpl"

    }

}
