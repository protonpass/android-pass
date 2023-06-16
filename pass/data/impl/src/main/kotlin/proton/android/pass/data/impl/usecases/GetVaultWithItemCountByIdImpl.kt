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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount
import javax.inject.Inject

class GetVaultWithItemCountByIdImpl @Inject constructor(
    private val getVaultById: GetVaultById,
    private val itemRepository: ItemRepository
) : GetVaultWithItemCountById {
    override fun invoke(userId: UserId?, shareId: ShareId): Flow<VaultWithItemCount> =
        getVaultById(userId = userId, shareId = shareId)
            .flatMapLatest { observeItemsForVault(it) }

    private fun observeItemsForVault(vault: Vault): Flow<VaultWithItemCount> =
        itemRepository.observeItemCount(listOf(vault.shareId))
            .map {
                val count = it.get(vault.shareId)
                    ?: throw IllegalStateException("Count should contain the ShareId")
                VaultWithItemCount(
                    vault = vault,
                    activeItemCount = count.activeItems,
                    trashedItemCount = count.trashedItems
                )
            }
}
