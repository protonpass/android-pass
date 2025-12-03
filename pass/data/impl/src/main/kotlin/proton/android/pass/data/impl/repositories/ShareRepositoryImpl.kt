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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.primary
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.UpdateVault
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.UpdateShareEvent
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.impl.crypto.ReencryptShareContents
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toEntity
import proton.android.pass.data.impl.extensions.toProto
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.extensions.toResponse
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.data.impl.util.TimeUtil.toDate
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.entity.NewVault
import proton.android.pass.domain.events.EventToken
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import proton_pass_vault_v1.VaultV1
import java.sql.Date
import javax.inject.Inject

@Suppress("TooManyFunctions", "LargeClass")
class ShareRepositoryImpl @Inject constructor(
    private val database: PassDatabase,
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val remoteShareDataSource: RemoteShareDataSource,
    private val localShareDataSource: LocalShareDataSource,
    private val reencryptShareContents: ReencryptShareContents,
    private val createVault: CreateVault,
    private val updateVault: UpdateVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val shareKeyRepository: ShareKeyRepository,
    private val userAccessDataRepository: UserAccessDataRepository,
    private val groupRepository: GroupRepository
) : ShareRepository {

    override suspend fun createVault(userId: UserId, vault: NewVault): Share {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val user = requireNotNull(userRepository.getUser(userId))
        val userPrimaryKey = requireNotNull(user.keys.primary()?.keyId?.id)

        val (request, shareKey) = runCatching {
            createVaultRequest(user, vault, userAddress)
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.w(TAG, "Error in CreateVaultRequest")
                PassLogger.w(TAG, it)
                throw it
            }
        )

        val createVaultResponse = remoteShareDataSource.createVault(userAddress.userId, request)
        val symmetricallyEncryptedKey = encryptionContextProvider.withEncryptionContextSuspendable {
            encrypt(shareKey.value())
        }
        val groupEmail = createVaultResponse.groupId?.let {
            groupRepository.retrieveGroup(userId, GroupId(it))
                ?.groupEmail
                ?: throw IllegalStateException("Group not found on vault create")
        }
        val encryptedContent = reencryptShareContents(createVaultResponse.content, shareKey)
        val responseAsEntity = createVaultResponse.toEntity(
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            encryptedContent = encryptedContent,
            isActive = true,
            groupEmail = groupEmail
        )

        val shareKeyEntity = ShareKeyEntity(
            rotation = 1,
            userId = userId.id,
            addressId = userAddress.addressId.id,
            shareId = createVaultResponse.shareId,
            key = request.encryptedVaultKey,
            createTime = createVaultResponse.createTime,
            symmetricallyEncryptedKey = symmetricallyEncryptedKey,
            isActive = true,
            userKeyId = userPrimaryKey
        )
        database.inTransaction("createVault") {
            localShareDataSource.upsertShares(listOf(responseAsEntity))
            shareKeyRepository.saveShareKeys(listOf(shareKeyEntity))
        }

        return encryptionContextProvider.withEncryptionContextSuspendable {
            responseAsEntity.toDomain(this@withEncryptionContextSuspendable)
        }
    }

    override suspend fun deleteVault(userId: UserId, shareId: ShareId) {
        remoteShareDataSource.deleteVault(userId, shareId)
        localShareDataSource.deleteShares(userId, setOf(shareId))
        refreshDefaultShareIfNeeded(userId, setOf(shareId))
    }

    override fun observeAllShares(userId: UserId, includeHidden: Boolean): Flow<List<Share>> =
        localShareDataSource.observeAllActiveSharesForUser(userId, includeHidden)
            .map { shares ->
                encryptionContextProvider.withEncryptionContextSuspendable {
                    shares.map { share ->
                        share.toDomain(this@withEncryptionContextSuspendable)
                    }
                }
            }

    override fun observeAllUsableShareIds(userId: UserId, includeHidden: Boolean): Flow<List<ShareId>> =
        localShareDataSource.observeUsableShareIds(userId, includeHidden)

    override fun observeSharesByType(
        userId: UserId,
        shareType: ShareType,
        includeHidden: Boolean
    ): Flow<List<Share>> = localShareDataSource.observeByType(userId, shareType, includeHidden)
        .map { shares ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                shares.map { share ->
                    share.toDomain(this@withEncryptionContextSuspendable)
                }
            }
        }

    @Suppress("LongMethod")
    override suspend fun refreshShares(userId: UserId, eventToken: EventToken?): RefreshSharesResult = coroutineScope {
        PassLogger.i(TAG, "Refreshing shares")

        val localShares = localShareDataSource.observeAllIncludingInactive(userId).first()
        val localSharesMap = localShares.associateBy { localShareEntity ->
            ShareId(localShareEntity.id)
        }
        val hadLocalSharesOnStart = localShares.isNotEmpty()

        val remoteShares = remoteShareDataSource.retrieveShares(userId, eventToken)
        val remoteSharesMap = remoteShares.associateBy { remoteShareResponse ->
            ShareId(remoteShareResponse.shareId)
        }

        // Update local share if needed
        val sharesToUpdate = remoteSharesMap.mapNotNull { (_, remoteShare) ->
            val localShare = localSharesMap[ShareId(remoteShare.shareId)]
            if (localShare != null && localShareNeedsUpdate(localShare, remoteShare)) {
                localShare to remoteShare
            } else {
                null
            }
        }.map { (localShare, remoteShare) ->
            async {
                val shareId = ShareId(remoteShare.shareId)
                val (encryptedContent, isActive) = getEncryptedContentAndActiveStatus(
                    localShare = localShare,
                    remoteShare = remoteShare,
                    shareId = shareId
                )

                remoteShare.toEntity(
                    userId = localShare.userId,
                    addressId = localShare.addressId,
                    encryptedContent = encryptedContent,
                    isActive = isActive,
                    groupEmail = localShare.groupEmail
                )
            }
        }.awaitAll()

        // Delete from the local data source the shares that are not in remote response
        val toDelete = localSharesMap.keys.subtract(remoteSharesMap.keys)

        refreshDefaultShareIfNeeded(userId, toDelete)

        if (sharesToUpdate.isNotEmpty() || toDelete.isNotEmpty()) {
            database.inTransaction("refreshShares") {
                if (sharesToUpdate.isNotEmpty()) {
                    PassLogger.i(TAG, "Updating ${sharesToUpdate.size} shares")
                    localShareDataSource.upsertShares(sharesToUpdate)
                }
                if (toDelete.isNotEmpty()) {
                    PassLogger.i(TAG, "Deleting ${toDelete.size} shares")
                    val deletedShareResult = localShareDataSource.deleteShares(userId, toDelete)
                    PassLogger.i(TAG, "Deleted $deletedShareResult shares")
                }
            }
        }

        val sharesNotInLocal = remoteShares
            // Filter out shares that are not in the local storage
            .filterNot { localSharesMap.containsKey(ShareId(it.shareId)) }
            .map { ShareId(it.shareId) }

        val inactiveLocalShares = localShares.filter { !it.isActive }.map { ShareId(it.id) }
        val remoteSharesToSave = remoteShares.filter {
            sharesNotInLocal.contains(ShareId(it.shareId)) ||
                inactiveLocalShares.contains(ShareId(it.shareId))
        }

        val storedShares = storeShares(userId, remoteSharesToSave)
        val inactiveNotInLocalShares = storedShares
            .filter { !it.isActive }
            .map { ShareId(it.id) }

        val newShares = storedShares.filter {
            it.isActive && sharesNotInLocal.contains(ShareId(it.id))
        }

        val allShareIds = remoteSharesMap.keys.filterNot {
            inactiveLocalShares.contains(it) || inactiveNotInLocalShares.contains(it)
        }.toSet()

        val wasFirstSync = !hadLocalSharesOnStart && allShareIds.isNotEmpty()

        PassLogger.i(TAG, "Refreshed shares")

        RefreshSharesResult(
            allShareIds = allShareIds,
            newShareIds = newShares.map { ShareId(it.id) }.toSet(),
            wasFirstSync = wasFirstSync
        )
    }

    override suspend fun refreshShare(
        userId: UserId,
        shareId: ShareId,
        eventToken: EventToken?
    ) {
        val shareResponse = remoteShareDataSource.retrieveShareById(userId, shareId, eventToken)
            ?: run {
                PassLogger.w(TAG, "Error fetching share from remote [shareId=${shareId.id}]")
                throw ShareNotAvailableError()
            }
        val localShare = localShareDataSource.getById(userId, shareId)
            ?: run {
                PassLogger.w(TAG, "Error fetching share from local [shareId=${shareId.id}]")
                throw ShareNotAvailableError()
            }

        val (encryptedContent, isActive) = getEncryptedContentAndActiveStatus(
            localShare = localShare,
            remoteShare = shareResponse,
            shareId = shareId
        )
        val updated = shareResponse.toEntity(
            userId = localShare.userId,
            addressId = localShare.addressId,
            encryptedContent = encryptedContent,
            isActive = isActive,
            groupEmail = localShare.groupEmail
        )
        localShareDataSource.upsertShares(listOf(updated))
    }

    override suspend fun getById(userId: UserId, shareId: ShareId): Share =
        localShareDataSource.getById(userId, shareId)?.let { entity ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                entity.toDomain(this@withEncryptionContextSuspendable)
            }
        } ?: throw ShareNotAvailableError()

    override fun observeById(userId: UserId, shareId: ShareId): Flow<Share> =
        localShareDataSource.observeById(userId, shareId)
            .map { shareEntity ->
                if (shareEntity == null) {
                    throw ShareNotAvailableError()
                }

                encryptionContextProvider.withEncryptionContextSuspendable {
                    shareEntity.toDomain(this@withEncryptionContextSuspendable)
                }
            }

    override suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        vault: NewVault
    ): Share {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val shareKey = shareKeyRepository.getLatestKeyForShare(shareId).first()
        val body = newVaultToBody(vault)
        val request = updateVault.createUpdateVaultRequest(shareKey, body).toRequest()
        val share = localShareDataSource.getById(userId, shareId)
        val response = runCatching {
            remoteShareDataSource.updateVault(userId, shareId, request)
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.w(TAG, "Error in updateVault")
                PassLogger.w(TAG, it)
                throw it
            }
        )

        val shareKeyAsEncryptionkey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(shareKey.key))
        }
        val encryptedContent = reencryptShareContents(response.content, shareKeyAsEncryptionkey)
        val shareEntity = response.toEntity(
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            encryptedContent = encryptedContent,
            isActive = true,
            groupEmail = share?.groupEmail
        )
        localShareDataSource.upsertShares(listOf(shareEntity))

        return encryptionContextProvider.withEncryptionContextSuspendable {
            shareEntity.toDomain(this@withEncryptionContextSuspendable)
        }
    }

    override suspend fun recreateShare(
        userId: UserId,
        shareId: ShareId,
        eventToken: EventToken?
    ) {
        val shareResponse = remoteShareDataSource.retrieveShareById(userId, shareId, eventToken)
            ?: run {
                PassLogger.w(TAG, "Error fetching share from remote [shareId=${shareId.id}]")
                throw ShareNotAvailableError()
            }
        localShareDataSource.deleteShares(userId, setOf(shareId))
        storeShares(userId, listOf(shareResponse))
    }

    override suspend fun deleteLocalSharesForUser(userId: UserId) = withContext(Dispatchers.IO) {
        localShareDataSource.deleteSharesForUser(userId)
    }

    override suspend fun deleteLocalShares(userId: UserId, list: List<ShareId>): Boolean =
        localShareDataSource.deleteShares(userId, list.toSet())

    override fun observeVaultCount(userId: UserId, includeHidden: Boolean): Flow<Int> =
        localShareDataSource.observeActiveVaultCount(userId, includeHidden)

    override suspend fun leaveVault(userId: UserId, shareId: ShareId) {
        remoteShareDataSource.leaveVault(userId, shareId)
        localShareDataSource.deleteShares(userId, setOf(shareId))
    }

    override suspend fun applyPendingShareEvent(userId: UserId, event: UpdateShareEvent) {
        onShareResponseEntity(userId, event) { (entity, _) ->
            localShareDataSource.upsertShares(listOf(entity))
        }
    }

    override suspend fun applyPendingShareEventKeys(userId: UserId, event: UpdateShareEvent) {
        onShareResponseEntity(userId, event) { (_, keys) ->
            shareKeyRepository.saveShareKeys(keys)
        }
    }

    override suspend fun getAddressForShareId(userId: UserId, shareId: ShareId): UserAddress {
        val entity = localShareDataSource.getById(userId, shareId)
            ?: throw ShareNotAvailableError()
        val address = userAddressRepository.getAddress(userId, AddressId(entity.addressId))
            ?: throw IllegalStateException("Could not find address for share")
        return address
    }

    override fun observeSharedWithMeIds(userId: UserId, includeHiddenVault: Boolean): Flow<List<ShareId>> =
        localShareDataSource.observeSharedWithMeIds(userId, includeHiddenVault)

    override fun observeSharedByMeIds(userId: UserId, includeHiddenVault: Boolean): Flow<List<ShareId>> =
        localShareDataSource.observeSharedByMeIds(userId, includeHiddenVault)

    override suspend fun batchChangeShareVisibility(userId: UserId, shareVisibilityChanges: Map<ShareId, Boolean>) {
        val response = remoteShareDataSource.batchChangeShareVisibility(userId, shareVisibilityChanges)
        storeShares(userId, response)
    }

    private suspend fun onShareResponseEntity(
        userId: UserId,
        event: UpdateShareEvent,
        block: suspend (Pair<ShareEntity, List<ShareKeyEntity>>) -> Unit
    ) {
        block(createShareResponseEntity(event.toResponse(), userId, event.groupEmail))
    }

    private suspend fun storeShares(userId: UserId, shares: List<ShareResponse>): List<ShareEntity> = coroutineScope {
        if (shares.isEmpty()) return@coroutineScope emptyList()
        PassLogger.i(TAG, "Fetching ShareKeys for ${shares.size} shares")
        val groups = if (shares.any { it.groupId != null }) {
            groupRepository.retrieveGroups(userId, forceRefresh = true)
        } else null
        val entities: List<Pair<ShareEntity, List<ShareKeyEntity>>> = shares.map { response ->
            val groupEmail = if (response.groupId != null) {
                groups?.find { it.id.id == response.groupId }
                    ?.groupEmail
                    ?: throw IllegalStateException("Group not found on storing shares")
            } else null
            async { createShareResponseEntity(response, userId, groupEmail) }
        }.awaitAll()

        val shareEntities = entities.map { (entity, _) -> entity }
        val shareKeyEntities = entities.map { (_, keys) -> keys }.flatten()

        if (shareEntities.isNotEmpty() || shareKeyEntities.isNotEmpty()) {
            database.inTransaction("storeShares") {
                // First, store the shares
                if (shareEntities.isNotEmpty()) {
                    PassLogger.i(TAG, "Storing ${shareEntities.size} shares")
                    localShareDataSource.upsertShares(shareEntities)
                }

                // Now that we have inserted the shares, we can safely insert the shareKeys
                if (shareKeyEntities.isNotEmpty()) {
                    PassLogger.i(TAG, "Storing ${shareKeyEntities.size} ShareKeys")
                    shareKeyRepository.saveShareKeys(shareKeyEntities)
                }
            }
        }

        shareEntities
    }

    private suspend fun createShareResponseEntity(
        shareResponse: ShareResponse,
        userId: UserId,
        groupEmail: String?
    ): Pair<ShareEntity, List<ShareKeyEntity>> {
        val userAddress = userAddressRepository.getAddresses(userId).primary()
            ?: throw IllegalStateException("Could not find PrimaryAddress")
        PassLogger.i(TAG, "Found primary user address")
        val shareId = ShareId(shareResponse.shareId)

        // First we fetch the shareKeys and not save them, in case the share has not been
        // inserted yet, as it would cause a FK mismatch in the database
        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userAddress.userId,
            addressId = userAddress.addressId,
            shareId = shareId,
            groupEmail = groupEmail,
            forceRefresh = false,
            shouldStoreLocally = false
        ).first()

        // Reencrypt the share contents
        val encryptionKey =
            getEncryptionKey(shareResponse.contentKeyRotation, shareKeys)
        val (encryptedContent, isActive) = when (encryptionKey) {
            EncryptionKeyStatus.NotFound -> null to true
            is EncryptionKeyStatus.Found -> {
                reencryptShareContents(shareResponse.content, encryptionKey.encryptionKey) to true
            }
            EncryptionKeyStatus.Inactive -> null to false
        }
        return shareResponse.toEntity(
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            encryptedContent = encryptedContent,
            isActive = isActive,
            groupEmail = groupEmail
        ) to shareKeys.map { shareKey ->
            ShareKeyEntity(
                rotation = shareKey.rotation,
                userId = userAddress.userId.id,
                addressId = userAddress.addressId.id,
                shareId = shareResponse.shareId,
                key = shareKey.responseKey,
                createTime = shareKey.createTime,
                symmetricallyEncryptedKey = shareKey.key,
                isActive = shareKey.isActive,
                userKeyId = shareKey.userKeyId
            )
        }
    }

    private suspend fun getEncryptedContentAndActiveStatus(
        localShare: ShareEntity,
        remoteShare: ShareResponse,
        shareId: ShareId
    ): Pair<EncryptedByteArray?, Boolean> {
        val contentChanged = localShare.content != remoteShare.content

        return if (contentChanged) {
            val shareKey = shareKeyRepository.getLatestKeyForShare(shareId).first()
            val encryptionKey = encryptionContextProvider.withEncryptionContextSuspendable {
                EncryptionKey(decrypt(shareKey.key))
            }
            when {
                remoteShare.contentKeyRotation == null -> null to true
                !shareKey.isActive -> null to false
                else -> reencryptShareContents(remoteShare.content, encryptionKey) to true
            }
        } else {
            localShare.encryptedContent to localShare.isActive
        }
    }

    private suspend fun getEncryptionKey(keyRotation: Long?, keys: List<ShareKey>): EncryptionKeyStatus {
        if (keyRotation == null) return EncryptionKeyStatus.NotFound

        val encryptionKey =
            keys.firstOrNull { it.rotation == keyRotation }
                ?: return EncryptionKeyStatus.NotFound
        if (!encryptionKey.isActive) {
            PassLogger.d(TAG, "Found key but it is not active")
            return EncryptionKeyStatus.Inactive
        }

        val decrypted =
            encryptionContextProvider.withEncryptionContextSuspendable { decrypt(encryptionKey.key) }
        return EncryptionKeyStatus.Found(EncryptionKey(decrypted))
    }

    private fun ShareEntity.toDomain(encryptionContext: EncryptionContext): Share = when (ShareType.from(targetType)) {
        ShareType.Item -> {
            Share.Item(
                id = ShareId(id),
                userId = UserId(userId),
                targetId = targetId,
                permission = SharePermission(permission),
                vaultId = VaultId(vaultId),
                groupId = groupId?.let(::GroupId),
                expirationTime = expirationTime?.let { Date(it) },
                createTime = createTime.toDate(),
                shareRole = ShareRole.fromValue(shareRoleId),
                isOwner = owner,
                memberCount = targetMembers,
                shared = shared,
                pendingInvites = pendingInvites,
                newUserInvitesReady = newUserInvitesReady,
                maxMembers = targetMaxMembers,
                canAutofill = canAutofill,
                shareFlags = ShareFlags(flags),
                groupEmail = groupEmail
            )
        }

        ShareType.Vault -> {
            encryptedContent?.let { vaultEncryptedContent ->
                encryptionContext.decrypt(vaultEncryptedContent)
                    .let(VaultV1.Vault::parseFrom)
                    .let { vault ->
                        Share.Vault(
                            id = ShareId(id),
                            userId = UserId(userId),
                            targetId = targetId,
                            permission = SharePermission(permission),
                            vaultId = VaultId(vaultId),
                            groupId = groupId?.let(::GroupId),
                            groupEmail = groupEmail,
                            expirationTime = expirationTime?.let { Date(it) },
                            createTime = createTime.toDate(),
                            shareRole = ShareRole.fromValue(shareRoleId),
                            isOwner = owner,
                            memberCount = targetMembers,
                            shared = shared,
                            pendingInvites = pendingInvites,
                            newUserInvitesReady = newUserInvitesReady,
                            maxMembers = targetMaxMembers,
                            canAutofill = canAutofill,
                            name = vault.name,
                            color = vault.display.color.toDomain(),
                            icon = vault.display.icon.toDomain(),
                            shareFlags = ShareFlags(flags)
                        )
                    }
            } ?: throw IllegalStateException("Vault share without encrypted content")
        }
    }

    private suspend fun createVaultRequest(
        user: User,
        vault: NewVault,
        userAddress: UserAddress
    ): Pair<CreateVaultRequest, EncryptionKey> {
        val body = newVaultToBody(vault)
        val (request, shareKey) = createVault.createVaultRequest(user, userAddress, body)
        return request.toRequest() to shareKey
    }

    private suspend fun newVaultToBody(vault: NewVault): VaultV1.Vault {
        val (name, description) = encryptionContextProvider.withEncryptionContextSuspendable {
            decrypt(vault.name) to decrypt(vault.description)
        }

        val display = VaultV1.VaultDisplayPreferences.newBuilder()
            .setColor(vault.color.toProto())
            .setIcon(vault.icon.toProto())
            .build()
        return VaultV1.Vault.newBuilder()
            .setName(name)
            .setDescription(description)
            .setDisplay(display)
            .build()
    }

    private fun localShareNeedsUpdate(localShare: ShareEntity, remoteShare: ShareResponse): Boolean = when {
        localShare.vaultId != remoteShare.vaultId -> true
        localShare.targetType != remoteShare.targetType -> true
        localShare.targetId != remoteShare.targetId -> true
        localShare.owner != remoteShare.owner -> true
        localShare.groupId != remoteShare.groupId -> true
        localShare.shareRoleId != remoteShare.shareRoleId -> true
        localShare.permission != remoteShare.permission -> true
        localShare.targetMembers != remoteShare.targetMembers -> true
        localShare.shared != remoteShare.shared -> true
        localShare.targetMaxMembers != remoteShare.targetMaxMembers -> true
        localShare.expirationTime != remoteShare.expirationTime -> true
        localShare.pendingInvites != remoteShare.pendingInvites -> true
        localShare.newUserInvitesReady != remoteShare.newUserInvitesReady -> true
        localShare.canAutofill != remoteShare.canAutofill -> true
        localShare.flags != remoteShare.flags -> true
        localShare.content != remoteShare.content -> true
        localShare.contentKeyRotation != remoteShare.contentKeyRotation -> true
        localShare.contentFormatVersion != remoteShare.contentFormatVersion -> true
        localShare.createTime != remoteShare.createTime -> true

        else -> false
    }

    private suspend fun refreshDefaultShareIfNeeded(userId: UserId, toDelete: Set<ShareId>) {
        if (toDelete.isEmpty()) return

        val defaultShareId = getSlSyncDefaultShareId(userId).value() ?: return
        if (toDelete.contains(defaultShareId)) {
            PassLogger.i(TAG, "Detected removal of SLSync ShareID. Refreshing")
            userAccessDataRepository.refresh(userId)
        }
    }

    private suspend fun getSlSyncDefaultShareId(userId: UserId): Option<ShareId> {
        val userData = userAccessDataRepository.observe(userId).firstOrNull()
        return when {
            userData == null -> None
            userData.simpleLoginSyncDefaultShareId.isBlank() -> None
            else -> ShareId(userData.simpleLoginSyncDefaultShareId).some()
        }
    }

    internal sealed interface EncryptionKeyStatus {
        data object NotFound : EncryptionKeyStatus
        data class Found(val encryptionKey: EncryptionKey) : EncryptionKeyStatus
        data object Inactive : EncryptionKeyStatus
    }

    private companion object {

        private const val TAG = "ShareRepositoryImpl"

    }

}
