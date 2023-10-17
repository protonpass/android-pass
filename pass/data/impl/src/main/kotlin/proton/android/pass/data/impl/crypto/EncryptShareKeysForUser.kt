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
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.usecases.EncryptInviteKeys
import proton.android.pass.crypto.api.usecases.EncryptedInviteShareKeyList
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareId
import proton.pass.domain.key.ShareKey
import javax.inject.Inject
import javax.inject.Singleton

interface EncryptShareKeysForUser {
    suspend operator fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        targetEmail: String,
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
    private val publicAddressRepository: PublicAddressRepository,
    private val encryptInviteKeys: EncryptInviteKeys,
) : EncryptShareKeysForUser {

    @Suppress("ReturnCount")
    override suspend fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        targetEmail: String,
    ): Result<EncryptedInviteShareKeyList> {
        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userAddress.userId,
            addressId = userAddress.addressId,
            shareId = shareId,
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
            ?: return Result.failure(IllegalStateException("No primary address key for invited user"))

        val targetUserAddress = runCatching {
            publicAddressRepository.getPublicAddress(userAddress.userId, targetEmail)
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.w(TAG, it, "Failed to get public addresses")
                return Result.failure(it)
            }
        )

        return runCatching {
            encryptInviteKeys(
                inviterAddressKey = inviterAddressKey,
                shareKeys = shareKeys,
                targetAddressKey = targetUserAddress.primaryKey.publicKey
            )
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = {
                PassLogger.w(TAG, it, "Failed to encrypt invite keys")
                Result.failure(it)
            }
        )
    }

    companion object {
        private const val TAG = "EncryptShareKeysForUserImpl"
    }
}
