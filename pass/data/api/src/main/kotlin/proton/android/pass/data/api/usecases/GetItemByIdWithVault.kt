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

package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.canUpdate
import proton.android.pass.domain.toPermissions

data class ItemWithVaultInfo(
    val item: Item,
    private val vaults: List<Vault>
) {
    val vault: Vault? = vaults.firstOrNull { vault ->
        vault.shareId == item.shareId
    }

    val hasMoreThanOneVault: Boolean = vaults.size > 1

    val canPerformItemActions: Boolean = vault?.role
        ?.toPermissions()
        ?.canUpdate()
        ?: false
}

interface GetItemByIdWithVault {
    operator fun invoke(shareId: ShareId, itemId: ItemId): Flow<ItemWithVaultInfo>
}
