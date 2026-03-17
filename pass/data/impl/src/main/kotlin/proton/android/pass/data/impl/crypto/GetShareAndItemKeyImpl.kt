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

import kotlinx.coroutines.flow.first
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.data.api.crypto.GetShareAndItemKey
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.impl.repositories.ItemKeyRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject

class GetShareAndItemKeyImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemKeyRepository: ItemKeyRepository
) : GetShareAndItemKey {

    override suspend fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        itemId: ItemId,
        decryptionKeyOverride: FolderKey?
    ): Pair<ShareKey, ItemKey> = shareRepository.getById(userAddress.userId, shareId).let { share ->
        val scope = when (share) {
            is Share.Item -> ItemKeyRepository.Scope.SharedItem
            is Share.Vault -> ItemKeyRepository.Scope.SharedVault(
                groupEmail = share.groupEmail,
                decryptionSource = decryptionSourceForVault(decryptionKeyOverride)
            )
        }
        itemKeyRepository.getLatestShareAndItemKey(
            userId = userAddress.userId,
            addressId = userAddress.addressId,
            shareId = shareId,
            itemId = itemId,
            scope = scope
        ).first()
    }

    private fun decryptionSourceForVault(decryptionKeyOverride: FolderKey?): ItemKeyRepository.VaultDecryptionSource =
        when (decryptionKeyOverride) {
            null -> ItemKeyRepository.VaultDecryptionSource.Share
            else -> ItemKeyRepository.VaultDecryptionSource.Folder(decryptionKeyOverride)
        }
}
