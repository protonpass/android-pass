/*
 * Copyright (c) 2023-2026 Proton AG
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

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.domain.key.ShareKey

interface ItemKeyRepository {

    sealed interface Scope {
        data object SharedItem : Scope

        data class SharedVault(
            val groupEmail: String?,
            val decryptionSource: VaultDecryptionSource = VaultDecryptionSource.Share
        ) : Scope
    }

    sealed interface VaultDecryptionSource {
        data object Share : VaultDecryptionSource
        data class Folder(val folderKey: FolderKey) : VaultDecryptionSource
    }

    fun getLatestItemKey(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        itemId: ItemId,
        scope: Scope.SharedVault
    ): Flow<ItemKey>

    fun getLatestShareAndItemKey(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        itemId: ItemId,
        scope: Scope
    ): Flow<Pair<ShareKey, ItemKey>>
}
