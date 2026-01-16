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

package proton.android.pass.data.api

import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.domain.Share

data object ItemFilterProcessor {

    fun <T : ItemData> removeDuplicates(array: Array<Pair<List<Share>, List<T>>>): List<T> {
        val shares = array.flatMap { it.first }
        val items = array.flatMap { it.second }

        val distinctShares = getDistinctShares(shares)
        val filteredItems = filterItemsByShares(items, distinctShares)

        return deduplicateItemsByUuid(filteredItems, distinctShares)
    }

    private fun getDistinctShares(sharesList: List<Share>): List<Share> {
        val (vaultShares, itemShares) = sharesList.partition { it is Share.Vault }

        val distinctVaultShares = vaultShares
            .groupBy { it.vaultId }
            .mapNotNull { (_, shares) ->
                shares.minWithOrNull(compareBy({ !it.isOwner }, { it.shareRole }))
            }

        return distinctVaultShares + itemShares
    }

    private fun <T : ItemData> filterItemsByShares(items: List<T>, shares: List<Share>): List<T> {
        val shareIds = shares.mapTo(HashSet()) { it.id }
        return items.filter { it.item.shareId in shareIds }
    }

    private fun <T : ItemData> deduplicateItemsByUuid(items: List<T>, shares: List<Share>): List<T> {
        val shareMap = shares.associateBy { it.id }

        return items
            .groupBy { it.item.itemUuid }
            .mapNotNull { (_, itemsWithSameUuid) ->
                itemsWithSameUuid.minWithOrNull(
                    compareBy(
                        { !(shareMap[it.item.shareId]?.isOwner ?: false) },
                        { shareMap[it.item.shareId]?.shareRole }
                    )
                )
            }
    }

}
