/*
 * Copyright (c) 2024-2026 Proton AG
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

import me.proton.core.key.domain.extension.primary
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.usecases.invites.EncryptInviteKeys
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteShareKeyList
import proton.android.pass.data.api.crypto.GetShareAndItemKey
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.impl.repositories.FolderKeyRepository
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface EncryptItemsKeysForUser {

    suspend operator fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        folderId: FolderId?,
        userAddress: UserAddress,
        targetEmail: String
    ): Result<EncryptedInviteShareKeyList>

}

class EncryptItemsKeysForUserImpl @Inject constructor(
    private val getAllKeysByAddress: GetAllKeysByAddress,
    private val encryptInviteKeys: EncryptInviteKeys,
    private val getShareAndItemKey: GetShareAndItemKey,
    private val folderKeyRepository: FolderKeyRepository
) : EncryptItemsKeysForUser {

    @Suppress("ReturnCount")
    override suspend fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        folderId: FolderId?,
        userAddress: UserAddress,
        targetEmail: String
    ): Result<EncryptedInviteShareKeyList> {
        val folderKey = resolveFolderKeyOverride(
            userAddress = userAddress,
            shareId = shareId,
            folderId = folderId
        ).getOrElse { return Result.failure(it) }
        val (_, itemKey) = getShareAndItemKey(
            userAddress = userAddress,
            shareId = shareId,
            itemId = itemId,
            decryptionKeyOverride = folderKey
        )

        val inviterAddressKey = userAddress.keys.primary()?.privateKey
            ?: return Result.failure(IllegalStateException("No primary address key for inviter user"))

        val targetUserKeys = getAllKeysByAddress(targetEmail).getOrElse {
            return Result.failure(it)
        }

        val targetAddressKey = targetUserKeys.firstOrNull()?.publicKey
            ?: return Result.failure(IllegalStateException("No primary address key for target user"))

        return safeRunCatching {
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

    private suspend fun resolveFolderKeyOverride(
        userAddress: UserAddress,
        shareId: ShareId,
        folderId: FolderId?
    ): Result<FolderKey?> = safeRunCatching {
        folderId ?: return@safeRunCatching null
        folderKeyRepository.getFolderKey(
            userId = userAddress.userId,
            shareId = shareId,
            folderId = folderId
        ) ?: throw IllegalStateException("Folder key not found for folderId=${folderId.id}")
    }

    private companion object {

        private const val TAG = "EncryptItemsKeysForUserImpl"

    }

}
