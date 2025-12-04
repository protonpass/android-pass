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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.ConfirmNewUserInvite
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.crypto.EncryptShareKeysForUser
import proton.android.pass.data.impl.crypto.NewUserInviteSignatureManager
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.data.impl.requests.invites.ConfirmInviteRequest
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfirmNewUserInviteImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val accountManager: AccountManager,
    private val shareKeyRepository: ShareKeyRepository,
    private val encryptShareKeysForUser: EncryptShareKeysForUser,
    private val signatureManager: NewUserInviteSignatureManager,
    private val userAddressRepository: UserAddressRepository,
    private val localShareDataSource: LocalShareDataSource,
    private val dispatchers: AppDispatchers
) : ConfirmNewUserInvite {
    override suspend fun invoke(shareId: ShareId, invite: VaultMember.NewUserInvitePending): Result<Unit> =
        withContext(dispatchers.io) {
            performConfirmation(shareId, invite)
        }

    private suspend fun performConfirmation(shareId: ShareId, invite: VaultMember.NewUserInvitePending): Result<Unit> {
        val userId = accountManager.getPrimaryUserId().firstOrNull()
        if (userId == null) {
            PassLogger.w(TAG, "No primary user")
            return Result.failure(IllegalStateException("No primary user"))
        }

        val body = createRequest(userId, shareId, invite).getOrElse {
            PassLogger.w(TAG, "Error creating confirmation request")
            PassLogger.w(TAG, it)
            return Result.failure(it)
        }

        val error = apiProvider.get<PasswordManagerApi>(userId).invoke {
            confirmInvite(
                shareId = shareId.id,
                inviteId = invite.newUserInviteId.value,
                request = body
            )
        }.exceptionOrNull

        return if (error != null) {
            PassLogger.w(TAG, "Error confirming invite")
            PassLogger.w(TAG, error)
            Result.failure(error)
        } else {
            Result.success(Unit)
        }
    }

    @Suppress("ReturnCount")
    private suspend fun createRequest(
        userId: UserId,
        shareId: ShareId,
        invite: VaultMember.NewUserInvitePending
    ): Result<ConfirmInviteRequest> {
        val share = localShareDataSource.getById(userId, shareId)
            ?: return Result.failure(IllegalStateException("No share with id $shareId"))

        val inviterUserAddress = safeRunCatching { userAddressRepository.getAddresses(userId) }
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

        val shareKeys = shareKeyRepository.getShareKeys(
            userId = inviterUserAddress.userId,
            addressId = inviterUserAddress.addressId,
            shareId = shareId,
            groupEmail = share.groupEmail,
            forceRefresh = true
        ).first()

        val vaultKey = shareKeys.maxByOrNull { it.rotation }
            ?: return Result.failure(IllegalStateException("ShareKey list is empty"))

        signatureManager.validate(
            inviterUserAddress = inviterUserAddress,
            signature = invite.signature,
            email = invite.email,
            inviteKey = vaultKey
        ).getOrElse {
            PassLogger.w(TAG, "Error validating invite signature")
            PassLogger.w(TAG, it)
            return Result.failure(it)
        }

        val encryptedKeys = encryptShareKeysForUser(
            userAddress = inviterUserAddress,
            shareId = shareId,
            targetEmail = invite.email,
            shareKeys = shareKeys
        ).getOrElse {
            PassLogger.w(TAG, "Error encrypting share keys")
            PassLogger.w(TAG, it)
            return Result.failure(it)
        }

        val body = ConfirmInviteRequest(
            keys = encryptedKeys.keys.map {
                InviteKeyRotation(
                    key = it.key,
                    keyRotation = it.keyRotation
                )
            }
        )

        return Result.success(body)
    }

    companion object {
        private const val TAG = "ConfirmNewUserInviteImpl"
    }
}
