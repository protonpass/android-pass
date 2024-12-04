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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.SessionUserId
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
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.UpdateShareEvent
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.impl.crypto.ReencryptShareContents
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toProto
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.extensions.toResponse
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.data.impl.util.TimeUtil.toDate
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.entity.NewVault
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.domain.shares.SharePendingInvite
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
    private val userAccessDataRepository: UserAccessDataRepository
) : ShareRepository {

    override suspend fun createVault(userId: SessionUserId, vault: NewVault): Share {
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
        val responseAsEntity = shareResponseToEntity(
            userAddress = userAddress,
            shareResponse = createVaultResponse,
            key = EncryptionKeyStatus.Found(shareKey)
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
        localShareDataSource.deleteShares(setOf(shareId))
        refreshDefaultShareIfNeeded(userId, setOf(shareId))
    }

    override fun observeAllShares(userId: SessionUserId): Flow<List<Share>> =
        localShareDataSource.observeAllActiveSharesForUser(userId)
            .map { shares ->
                encryptionContextProvider.withEncryptionContextSuspendable {
                    shares.map { share ->
                        share.toDomain(this@withEncryptionContextSuspendable)
                    }
                }
            }

    override fun observeSharesByType(
        userId: UserId,
        shareType: ShareType,
        isActive: Boolean?
    ): Flow<List<Share>> = localShareDataSource.observeByType(userId, shareType, isActive)
        .map { shares ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                shares.map { share ->
                    share.toDomain(this@withEncryptionContextSuspendable)
                }
            }
        }

    @Suppress("LongMethod")
    override suspend fun refreshShares(userId: UserId): RefreshSharesResult = coroutineScope {
        PassLogger.i(TAG, "Refreshing shares")

        val localShares = localShareDataSource.getAllSharesForUser(userId).first()
        val localSharesMap = localShares.associateBy { localShareEntity ->
            ShareId(localShareEntity.id)
        }
        val hadLocalSharesOnStart = localShares.isNotEmpty()

        val remoteShares = remoteShareDataSource.getShares(userId)
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
        }.map { (localShare, remoteShare) -> updateEntityWithResponse(localShare, remoteShare) }

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
                    val deletedShareResult = localShareDataSource.deleteShares(toDelete)
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
            sharesNotInLocal.contains(ShareId(it.shareId)) || inactiveLocalShares.contains(
                ShareId(
                    it.shareId
                )
            )
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

    override suspend fun refreshShare(userId: UserId, shareId: ShareId) {
        val shareResponse = remoteShareDataSource.fetchShareById(userId, shareId)
            ?: run {
                PassLogger.w(TAG, "Error fetching share from remote [shareId=${shareId.id}]")
                throw ShareNotAvailableError()
            }
        val localShare = localShareDataSource.getById(userId, shareId)
            ?: run {
                PassLogger.w(TAG, "Error fetching share from local [shareId=${shareId.id}]")
                throw ShareNotAvailableError()
            }

        val updated = updateEntityWithResponse(localShare, shareResponse)
        localShareDataSource.upsertShares(listOf(updated))
    }

    @Suppress("ReturnCount")
    override suspend fun getById(userId: UserId, shareId: ShareId): Share {
        // Check local
        var shareEntity: ShareEntity? = localShareDataSource.getById(userId, shareId)
        if (shareEntity == null) {
            // Check remote
            val fetchedShare = remoteShareDataSource.fetchShareById(userId, shareId)
            val shareResponse = fetchedShare ?: run {
                PassLogger.w(TAG, "Error fetching share from remote [shareId=${shareId.id}]")
                throw ShareNotAvailableError()
            }
            val storedShares: List<ShareEntity> = storeShares(
                userId = userId,
                shares = listOf(shareResponse)
            )
            shareEntity = storedShares.first()
        }

        return encryptionContextProvider.withEncryptionContextSuspendable {
            shareEntity.toDomain(this@withEncryptionContextSuspendable)
        }
    }

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
        val response = kotlin.runCatching {
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
        val shareEntity = shareResponseToEntity(
            userAddress = userAddress,
            shareResponse = response,
            key = EncryptionKeyStatus.Found(shareKeyAsEncryptionkey)
        )
        localShareDataSource.upsertShares(listOf(shareEntity))

        return encryptionContextProvider.withEncryptionContextSuspendable {
            shareEntity.toDomain(this@withEncryptionContextSuspendable)
        }
    }

    override suspend fun deleteSharesForUser(userId: UserId) = withContext(Dispatchers.IO) {
        localShareDataSource.deleteSharesForUser(userId)
    }

    override fun observeVaultCount(userId: UserId): Flow<Int> = localShareDataSource.observeActiveVaultCount(userId)

    override suspend fun leaveVault(userId: UserId, shareId: ShareId) {
        remoteShareDataSource.leaveVault(userId, shareId)
        localShareDataSource.deleteShares(setOf(shareId))
    }

    override suspend fun applyUpdateShareEvent(
        userId: UserId,
        shareId: ShareId,
        event: UpdateShareEvent
    ) {
        val asResponse = event.toResponse()

        storeShares(userId, listOf(asResponse))
    }

    override suspend fun applyPendingShareEvent(userId: UserId, event: UpdateShareEvent) {
        onShareResponseEntity(userId, event) { shareResponseEntity ->
            localShareDataSource.upsertShares(listOf(shareResponseEntity.entity))
        }
    }

    override suspend fun applyPendingShareEventKeys(userId: UserId, event: UpdateShareEvent) {
        onShareResponseEntity(userId, event) { shareResponseEntity ->
            shareKeyRepository.saveShareKeys(shareResponseEntity.keys)
        }
    }

    override suspend fun getAddressForShareId(userId: UserId, shareId: ShareId): UserAddress {
        val entity = localShareDataSource.getById(userId, shareId)
            ?: throw ShareNotAvailableError()
        val address = userAddressRepository.getAddress(userId, AddressId(entity.addressId))
            ?: throw IllegalStateException("Could not find address for share")
        return address
    }

    override fun observeShareMembers(
        userId: UserId,
        shareId: ShareId,
        userEmail: String?
    ): Flow<List<ShareMember>> = flow {
        remoteShareDataSource.getShareMembers(userId, shareId)
            .map { shareMemberResponse ->
                ShareMember(
                    email = shareMemberResponse.userEmail,
                    shareId = ShareId(shareMemberResponse.shareId),
                    username = shareMemberResponse.userName,
                    isCurrentUser = shareMemberResponse.userEmail == userEmail,
                    isOwner = shareMemberResponse.owner ?: false,
                    role = shareMemberResponse.shareRoleId
                        ?.let(ShareRole::fromValue)
                        ?: ShareRole.fromValue(ShareRole.SHARE_ROLE_READ)
                )
            }
            .also { shareMembers ->
                emit(shareMembers)
            }
    }

    override fun observeSharePendingInvites(userId: UserId, shareId: ShareId): Flow<List<SharePendingInvite>> = flow {
        remoteShareDataSource.getSharePendingInvites(userId, shareId)
            .let { sharePendingInviteResponse ->
                buildList {
                    sharePendingInviteResponse.invites
                        .map { actualUserPendingInvite ->
                            SharePendingInvite.ExistingUser(
                                email = actualUserPendingInvite.invitedEmail,
                                inviteId = InviteId(actualUserPendingInvite.inviteId)
                            )
                        }
                        .also(::addAll)

                    sharePendingInviteResponse.newUserInvites
                        .map { newUserPendingInvite ->
                            SharePendingInvite.NewUser(
                                email = newUserPendingInvite.invitedEmail,
                                inviteId = NewUserInviteId(newUserPendingInvite.newUserInviteId),
                                role = ShareRole.fromValue(newUserPendingInvite.shareRoleId),
                                inviteState = SharePendingInvite.NewUser.InviteState.fromValue(
                                    value = newUserPendingInvite.state
                                )
                            )
                        }
                        .also(::addAll)
                }.also { sharePendingInvites ->
                    emit(sharePendingInvites)
                }
            }

    }

    private suspend fun onShareResponseEntity(
        userId: UserId,
        event: UpdateShareEvent,
        block: suspend (ShareResponseEntity) -> Unit
    ) {
        block(createShareResponseEntity(event.toResponse(), userId))
    }

    private suspend fun storeShares(userId: UserId, shares: List<ShareResponse>): List<ShareEntity> = coroutineScope {
        if (shares.isEmpty()) return@coroutineScope emptyList()

        PassLogger.i(TAG, "Fetching ShareKeys for ${shares.size} shares")
        val entities: List<ShareResponseEntity> = shares.map { response ->
            async { createShareResponseEntity(response, userId) }
        }.awaitAll()

        val shareEntities = entities.map { shareResponseEntity -> shareResponseEntity.entity }
        val shareKeyEntities = entities.map { shareResponseEntity -> shareResponseEntity.keys }
            .flatten()

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

    private suspend fun createShareResponseEntity(shareResponse: ShareResponse, userId: UserId): ShareResponseEntity {
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
            forceRefresh = false,
            shouldStoreLocally = false
        ).first()

        // Reencrypt the share contents
        val encryptionKey =
            getEncryptionKey(shareResponse.contentKeyRotation, shareKeys)
        return ShareResponseEntity(
            response = shareResponse,
            entity = shareResponseToEntity(
                userAddress,
                shareResponse,
                encryptionKey
            ),
            keys = shareKeys.map { shareKey ->
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
        )
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

    private fun shareResponseToEntity(
        userAddress: UserAddress,
        shareResponse: ShareResponse,
        key: EncryptionKeyStatus
    ): ShareEntity {

        val (encryptedContent, isActive) = when (key) {
            EncryptionKeyStatus.NotFound -> null to true
            is EncryptionKeyStatus.Found -> {
                reencryptShareContents(shareResponse.content, key.encryptionKey) to true
            }

            EncryptionKeyStatus.Inactive -> null to false
        }
        return ShareEntity(
            id = shareResponse.shareId,
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            vaultId = shareResponse.vaultId,
            targetType = shareResponse.targetType,
            targetId = shareResponse.targetId,
            permission = shareResponse.permission,
            content = shareResponse.content,
            contentKeyRotation = shareResponse.contentKeyRotation,
            contentFormatVersion = shareResponse.contentFormatVersion,
            expirationTime = shareResponse.expirationTime,
            createTime = shareResponse.createTime,
            encryptedContent = encryptedContent,
            isActive = isActive,
            shareRoleId = shareResponse.shareRoleId,
            owner = shareResponse.owner,
            targetMembers = shareResponse.targetMembers,
            shared = shareResponse.shared,
            targetMaxMembers = shareResponse.targetMaxMembers,
            newUserInvitesReady = shareResponse.newUserInvitesReady,
            pendingInvites = shareResponse.pendingInvites,
            canAutofill = shareResponse.canAutofill
        )
    }

    private fun ShareEntity.toDomain(encryptionContext: EncryptionContext): Share = when (ShareType.from(targetType)) {
        ShareType.Item -> {
            Share.Item(
                id = ShareId(id),
                userId = UserId(userId),
                targetId = targetId,
                permission = SharePermission(permission),
                vaultId = VaultId(vaultId),
                expirationTime = expirationTime?.let { Date(it) },
                createTime = createTime.toDate(),
                shareRole = ShareRole.fromValue(shareRoleId),
                isOwner = owner,
                memberCount = targetMembers,
                shared = shared,
                pendingInvites = pendingInvites,
                newUserInvitesReady = newUserInvitesReady,
                maxMembers = targetMaxMembers,
                canAutofill = canAutofill
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
                            icon = vault.display.icon.toDomain()
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
        localShare.owner != remoteShare.owner -> true
        localShare.shareRoleId != remoteShare.shareRoleId -> true
        localShare.permission != remoteShare.permission -> true
        localShare.targetMembers != remoteShare.targetMembers -> true
        localShare.shared != remoteShare.shared -> true
        localShare.targetMaxMembers != remoteShare.targetMaxMembers -> true
        localShare.expirationTime != remoteShare.expirationTime -> true
        localShare.pendingInvites != remoteShare.pendingInvites -> true
        localShare.newUserInvitesReady != remoteShare.newUserInvitesReady -> true
        localShare.canAutofill != remoteShare.canAutofill -> true

        else -> false
    }

    private fun updateEntityWithResponse(entity: ShareEntity, response: ShareResponse): ShareEntity = entity.copy(
        owner = response.owner,
        shareRoleId = response.shareRoleId,
        targetMembers = response.targetMembers,
        shared = response.shared,
        permission = response.permission,
        targetMaxMembers = response.targetMaxMembers,
        expirationTime = response.expirationTime,
        newUserInvitesReady = response.newUserInvitesReady,
        pendingInvites = response.pendingInvites
    )

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

    internal data class ShareResponseEntity(
        val response: ShareResponse,
        val entity: ShareEntity,
        val keys: List<ShareKeyEntity>
    )

    internal sealed interface EncryptionKeyStatus {
        data object NotFound : EncryptionKeyStatus
        data class Found(val encryptionKey: EncryptionKey) : EncryptionKeyStatus
        data object Inactive : EncryptionKeyStatus
    }

    private companion object {

        private const val TAG = "ShareRepositoryImpl"

    }

}
