/*
 * Copyright (c) 2025 Proton AG
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.GroupInviteRepository
import proton.android.pass.data.impl.crypto.EncryptGroupInviteKeys
import proton.android.pass.data.impl.crypto.ReencryptGroupInviteContents
import proton.android.pass.data.impl.db.entities.GroupInviteEntity
import proton.android.pass.data.impl.db.entities.GroupInviteKeyEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.GroupInviteAndKeysEntity
import proton.android.pass.data.impl.local.LocalGroupInviteDataSource
import proton.android.pass.data.impl.remote.groups.RemoteGroupInviteDataSource
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.data.impl.responses.invites.GroupInviteApiModel
import proton.android.pass.data.impl.responses.invites.KeyApiModel
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.events.EventToken
import proton.android.pass.log.api.PassLogger
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class GroupInviteRepositoryImpl @Inject constructor(
    private val remoteGroupInviteDataSource: RemoteGroupInviteDataSource,
    private val localGroupInviteDataSource: LocalGroupInviteDataSource,
    private val reencryptGroupInviteContents: ReencryptGroupInviteContents,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val encryptGroupInviteKeys: EncryptGroupInviteKeys
) : GroupInviteRepository {

    override fun observePendingGroupInvites(
        userId: UserId,
        forceRefresh: Boolean,
        eventToken: EventToken?
    ): Flow<List<PendingInvite>> = localGroupInviteDataSource.observeAllInvites(userId)
        .onStart {
            if (forceRefresh) {
                refreshInvites(userId, eventToken)
            }
        }
        .map { entities ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                entities.map { it.toDomain(this) }
            }
        }

    override fun observePendingGroupInvite(userId: UserId, inviteId: InviteId): Flow<PendingInvite?> =
        localGroupInviteDataSource.observeInvite(userId, inviteId)
            .map {
                it?.let { entity ->
                    encryptionContextProvider.withEncryptionContextSuspendable {
                        entity.toDomain(this)
                    }
                }
            }

    override suspend fun acceptGroupInvite(
        userId: UserId,
        inviteId: InviteId,
        inviteToken: InviteToken
    ) {
        val inviteWithKeys = localGroupInviteDataSource.getInviteWithKeys(userId, inviteId)
            ?: throw IllegalStateException("Could not find the invite: ${inviteId.value}")

        val keys: List<InviteKeyRotation> = encryptGroupInviteKeys(
            userId = userId,
            invite = inviteWithKeys
        )
        remoteGroupInviteDataSource.acceptGroupInvite(userId, inviteToken, keys)
        localGroupInviteDataSource.removeInvite(userId, inviteId)
    }

    override suspend fun rejectGroupInvite(userId: UserId, inviteToken: InviteToken) {
        remoteGroupInviteDataSource.rejectGroupInvite(userId, inviteToken)
    }

    private suspend fun refreshInvites(userId: UserId, eventToken: EventToken?): Boolean = coroutineScope {
        PassLogger.i(TAG, "Refresh invites started")
        val deferredRemoteInvites: Deferred<List<GroupInviteApiModel>> =
            async { fetchAllRemoteInvites(userId, eventToken) }
        deferredRemoteInvites.invokeOnCompletion {
            if (it != null) {
                PassLogger.w(TAG, it)
            } else {
                PassLogger.i(TAG, "Fetched remote invites")
            }
        }
        val deferredLocalInvites: Deferred<List<GroupInviteEntity>> =
            async { localGroupInviteDataSource.observeAllInvites(userId).first() }
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
            remoteInvites.none { remote -> remote.inviteId == local.inviteId }
        }
        if (deletedInvites.isNotEmpty()) {
            PassLogger.i(TAG, "Deleting ${deletedInvites.size} invites")
            localGroupInviteDataSource.removeInvites(deletedInvites)
        }

        // Insert new invites
        val newInvites = remoteInvites.filter { remote ->
            localInvites.none { local -> local.inviteId == remote.inviteId }
        }
        val hasNewInvites = newInvites.isNotEmpty()

        val invitesWithKeys: List<GroupInviteAndKeysEntity> = newInvites
            .map { invite -> inviteAndKeysEntity(invite, userId) }

        if (invitesWithKeys.isNotEmpty()) {
            PassLogger.i(TAG, "Inserting ${invitesWithKeys.size} invites")
            localGroupInviteDataSource.storeInvites(invitesWithKeys)
        }
        hasNewInvites
    }

    private suspend fun fetchAllRemoteInvites(userId: UserId, eventToken: EventToken?): List<GroupInviteApiModel> {
        val allInvites = mutableListOf<GroupInviteApiModel>()
        var lastToken: String? = null

        do {
            val response =
                remoteGroupInviteDataSource.retrievePendingGroupInvites(userId, lastToken, eventToken)
            allInvites.addAll(response.invites)
            lastToken = response.lastId
        } while (lastToken != null)

        return allInvites
    }

    private suspend fun inviteAndKeysEntity(invite: GroupInviteApiModel, userId: UserId): GroupInviteAndKeysEntity {
        val reencryptedInvite = reencryptGroupInviteContents(userId, invite)
        return GroupInviteAndKeysEntity(
            groupInviteEntity = invite.toEntity(userId, reencryptedInvite.encryptedContent),
            inviteKeys = invite.keys.map { it.toEntity(invite.inviteId, invite.createTime) }
        )
    }

    private fun GroupInviteEntity.toDomain(encryptionContext: EncryptionContext): PendingInvite =
        when (ShareType.from(targetType)) {
            ShareType.Item -> {
                PendingInvite.GroupItem(
                    inviteId = InviteId(inviteId),
                    inviteToken = InviteToken(inviteToken),
                    inviterEmail = inviterEmail,
                    invitedAddressId = invitedAddressId,
                    invitedGroupId = invitedGroupId,
                    invitedEmail = invitedEmail,
                    inviterUserId = inviterUserId,
                    targetId = targetId,
                    remindersSent = remindersSent
                )
            }

            ShareType.Vault -> {
                val content = encryptionContext.decrypt(encryptedContent)
                val decoded = VaultV1.Vault.parseFrom(content)

                PendingInvite.GroupVault(
                    inviteId = InviteId(inviteId),
                    inviteToken = InviteToken(inviteToken),
                    inviterEmail = inviterEmail,
                    invitedAddressId = invitedAddressId,
                    memberCount = memberCount,
                    itemCount = itemCount,
                    name = decoded.name,
                    icon = decoded.display.icon.toDomain(),
                    color = decoded.display.color.toDomain(),
                    invitedGroupId = invitedGroupId,
                    invitedEmail = invitedEmail,
                    inviterUserId = inviterUserId,
                    targetId = targetId,
                    remindersSent = remindersSent
                )
            }
        }

    private fun GroupInviteApiModel.toEntity(
        userId: UserId,
        reencryptedInviteContent: EncryptedByteArray
    ): GroupInviteEntity = GroupInviteEntity(
        inviteId = inviteId,
        userId = userId.id,
        inviterUserId = inviterUserId,
        inviterEmail = inviterEmail,
        invitedGroupId = invitedGroupId,
        invitedEmail = invitedEmail,
        targetType = targetType,
        targetId = targetId,
        remindersSent = remindersSent,
        inviteToken = inviteToken,
        invitedAddressId = invitedAddressId,
        memberCount = vaultData?.memberCount ?: 0,
        itemCount = vaultData?.itemCount ?: 0,
        shareContent = vaultData?.content.orEmpty(),
        shareContentKeyRotation = vaultData?.contentKeyRotation ?: -1L,
        shareContentFormatVersion = vaultData?.contentFormatVersion ?: -1,
        createTime = createTime,
        encryptedContent = reencryptedInviteContent
    )

    private fun KeyApiModel.toEntity(inviteId: String, createTime: Long): GroupInviteKeyEntity = GroupInviteKeyEntity(
        inviteId = inviteId,
        key = key,
        keyRotation = keyRotation,
        createTime = createTime
    )

    companion object {
        private const val TAG = "GroupInviteRepositoryImpl"
    }
}
