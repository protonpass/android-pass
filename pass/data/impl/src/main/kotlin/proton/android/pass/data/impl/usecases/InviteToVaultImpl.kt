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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.impl.crypto.EncryptShareKeysForUser
import proton.android.pass.data.impl.crypto.NewUserInviteSignatureManager
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.remote.RemoteInviteDataSource
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.data.impl.requests.CreateInviteKey
import proton.android.pass.data.impl.requests.CreateInviteRequest
import proton.android.pass.data.impl.requests.CreateNewUserInviteRequest
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class InviteToVaultImpl @Inject constructor(
    private val userAddressRepository: UserAddressRepository,
    private val accountManager: AccountManager,
    private val encryptShareKeysForUser: EncryptShareKeysForUser,
    private val shareKeyRepository: ShareKeyRepository,
    private val remoteInviteDataSource: RemoteInviteDataSource,
    private val localShareDataSource: LocalShareDataSource,
    private val newUserInviteSignatureManager: NewUserInviteSignatureManager
) : InviteToVault {

    @Suppress("ReturnCount")
    override suspend fun invoke(
        userId: UserId?,
        targetEmail: String,
        shareId: ShareId,
        shareRole: ShareRole,
        userMode: InviteToVault.UserMode
    ): Result<Unit> {
        val id = userId ?: run {
            val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
            if (primaryUserId == null) {
                PassLogger.w(TAG, "No primary user")
                return Result.failure(IllegalStateException("No primary user"))
            }
            primaryUserId
        }

        val share = localShareDataSource.getById(id, shareId)
            ?: return Result.failure(IllegalStateException("No share with id $shareId"))

        val inviterUserAddress = runCatching { userAddressRepository.getAddresses(id) }
            .fold(
                onSuccess = { addresses ->
                    val address = addresses.firstOrNull { it.addressId.id == share.addressId }
                        ?: return Result.failure(IllegalStateException("No primary address for inviter user"))
                    address
                },
                onFailure = {
                    PassLogger.w(TAG, "Failed to get user addresses")
                    PassLogger.w(TAG, it)
                    return Result.failure(it)
                }
            )

        return when (userMode) {
            InviteToVault.UserMode.ExistingUser -> buildExistingUserRequest(
                shareId = shareId,
                address = inviterUserAddress,
                targetEmail = targetEmail,
                shareRole = shareRole,
            ).mapCatching { request ->
                remoteInviteDataSource.sendInvite(id, shareId, request)
            }

            InviteToVault.UserMode.NewUser -> buildNewUserRequest(
                userId = id,
                shareId = shareId,
                address = inviterUserAddress,
                targetEmail = targetEmail,
                shareRole = shareRole,
            ).mapCatching { request ->
                remoteInviteDataSource.sendNewUserInvite(id, shareId, request)
            }
        }
    }

    @Suppress("ReturnCount")
    private suspend fun buildExistingUserRequest(
        shareId: ShareId,
        address: UserAddress,
        targetEmail: String,
        shareRole: ShareRole
    ): Result<CreateInviteRequest> {
        val encryptedKeys = encryptShareKeysForUser(
            userAddress = address,
            shareId = shareId,
            targetEmail = targetEmail,
        ).getOrElse {
            return Result.failure(it)
        }

        val request = CreateInviteRequest(
            keys = encryptedKeys.keys.map {
                CreateInviteKey(
                    key = it.key,
                    keyRotation = it.keyRotation
                )
            },
            email = targetEmail,
            targetType = TARGET_TYPE_VAULT,
            shareRoleId = shareRole.value
        )

        return Result.success(request)
    }

    private suspend fun buildNewUserRequest(
        userId: UserId,
        shareId: ShareId,
        address: UserAddress,
        targetEmail: String,
        shareRole: ShareRole
    ): Result<CreateNewUserInviteRequest> {

        val vaultKeyList = shareKeyRepository.getShareKeys(
            userId = userId,
            addressId = address.addressId,
            shareId = shareId,
            forceRefresh = false
        ).firstOrNull() ?: return Result.failure(IllegalStateException("No ShareKey found for share"))

        val vaultKey = vaultKeyList.maxByOrNull { it.rotation }
            ?: return Result.failure(IllegalStateException("ShareKey list is empty"))

        val signature = newUserInviteSignatureManager.create(
            inviterUserAddress = address,
            email = targetEmail,
            vaultKey = vaultKey
        ).getOrElse {
            PassLogger.w(TAG, "Failed to create new user invite signature")
            PassLogger.w(TAG, it)
            return Result.failure(it)
        }

        val request = CreateNewUserInviteRequest(
            email = targetEmail,
            targetType = TARGET_TYPE_VAULT,
            shareRoleId = shareRole.value,
            signature = signature
        )
        return Result.success(request)
    }

    companion object {
        private const val TAG = "InviteToVaultImpl"
        private const val TARGET_TYPE_VAULT = 1
    }
}
