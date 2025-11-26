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

package proton.android.pass.data.impl.crypto

import kotlinx.coroutines.flow.first
import me.proton.core.key.domain.extension.primary
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.usecases.invites.EncryptInviteKeys
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteShareKeyList
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

interface EncryptShareKeysForUser {
    suspend operator fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        groupEmail: String?,
        targetEmail: String
    ): Result<EncryptedInviteShareKeyList>

    suspend operator fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        targetEmail: String,
        shareKeys: List<ShareKey>
    ): Result<EncryptedInviteShareKeyList>
}

@Singleton
class EncryptShareKeysForUserImpl @Inject constructor(
    private val shareKeyRepository: ShareKeyRepository,
    private val getAllKeysByAddress: GetAllKeysByAddress,
    private val encryptInviteKeys: EncryptInviteKeys
) : EncryptShareKeysForUser {

    @Suppress("ReturnCount")
    override suspend fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        groupEmail: String?,
        targetEmail: String
    ): Result<EncryptedInviteShareKeyList> {
        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userAddress.userId,
            addressId = userAddress.addressId,
            shareId = shareId,
            groupEmail = groupEmail,
            forceRefresh = true
        ).first()

        return invoke(
            userAddress = userAddress,
            shareId = shareId,
            targetEmail = targetEmail,
            shareKeys = shareKeys
        )
    }

    override suspend fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        targetEmail: String,
        shareKeys: List<ShareKey>
    ): Result<EncryptedInviteShareKeyList> {
        val inviterAddressKey = userAddress.keys.primary()?.privateKey
            ?: return Result.failure(IllegalStateException("No primary address key for inviter user"))

        val targetUserKeys = getAllKeysByAddress(targetEmail).getOrElse {
            return Result.failure(it)
        }

        val targetAddressKey = targetUserKeys.firstOrNull()?.publicKey
            ?: return Result.failure(IllegalStateException("No primary address key for target user"))

        return runCatching {
            encryptInviteKeys(
                inviterAddressKey = inviterAddressKey,
                inviteKeys = shareKeys,
                targetAddressKey = targetAddressKey
            )
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = {
                PassLogger.w(TAG, "Failed to encrypt invite keys")
                PassLogger.w(TAG, it)
                Result.failure(it)
            }
        )
    }

    private companion object {

        private const val TAG = "EncryptShareKeysForUserImpl"

    }

}
