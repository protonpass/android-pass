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
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.sorted
import javax.inject.Inject

class ObserveVaultsWithItemCountImpl @Inject constructor(
    private val observeVaults: ObserveVaults,
    private val itemRepository: ItemRepository
) : ObserveVaultsWithItemCount {

    override fun invoke(includeHidden: Boolean): Flow<List<VaultWithItemCount>> =
        observeVaults(includeHidden = includeHidden).flatMapLatest { result ->
            observeItemCounts(result)
        }

    private fun observeItemCounts(vaultList: List<Vault>): Flow<List<VaultWithItemCount>> =
        itemRepository.observeItemCount(
            shareIds = vaultList.map { it.shareId }
        ).map { count -> mapVaults(vaultList, count) }

    private fun mapVaults(vaultList: List<Vault>, count: Map<ShareId, ShareItemCount>): List<VaultWithItemCount> {
        val res = vaultList.map { vault ->
            val itemsForShare = count[vault.shareId]
                ?: throw IllegalStateException("Could not find ItemCount for share")

            VaultWithItemCount(
                vault = vault,
                activeItemCount = itemsForShare.activeItems,
                trashedItemCount = itemsForShare.trashedItems
            )
        }.sorted()
        return res
    }

}
