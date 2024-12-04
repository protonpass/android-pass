/*
 * Copyright (c) 2024 Proton AG
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
import proton.android.pass.crypto.api.usecases.EncryptInviteKeys
import proton.android.pass.crypto.api.usecases.EncryptedInviteShareKeyList
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface EncryptItemsKeysForUser {

    suspend operator fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        userAddress: UserAddress,
        targetEmail: String
    ): Result<EncryptedInviteShareKeyList>

}

class EncryptItemsKeysForUserImpl @Inject constructor(
    private val shareKeyRepository: ShareKeyRepository,
    private val getAllKeysByAddress: GetAllKeysByAddress,
    private val encryptInviteKeys: EncryptInviteKeys
) : EncryptItemsKeysForUser {

    @Suppress("ReturnCount")
    override suspend fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        userAddress: UserAddress,
        targetEmail: String
    ): Result<EncryptedInviteShareKeyList> {
        val itemKey = shareKeyRepository.getLatestKeyForShare(shareId)
            .first()
            .let { shareKey ->
                ItemKey(
                    rotation = shareKey.rotation,
                    key = shareKey.key,
                    responseKey = shareKey.responseKey
                )
            }

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
                inviteKeys = listOf(itemKey),
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

        private const val TAG = "EncryptItemsKeysForUserImpl"

    }

}
