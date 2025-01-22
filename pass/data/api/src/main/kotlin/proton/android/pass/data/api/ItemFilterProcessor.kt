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
        val shares = array.map { (share, _) -> share }.flatten()
        val items = array.map { (_, item) -> item }.flatten()

        return filterItemsByShares(
            items = items,
            shares = getDistinctShares(shares)
        )
    }

    private fun getDistinctShares(sharesList: List<Share>): List<Share> = sharesList
        .groupBy { share -> share.vaultId }
        .mapValues { (_, shares) ->
            compareBy({ share -> !share.isOwner }, Share::shareRole)
                .let(shares::sortedWith)
                .first()
        }
        .values
        .toList()

    private fun <T : ItemData> filterItemsByShares(shares: List<Share>, items: List<T>): List<T> = shares
        .map { share -> share.id }
        .toSet()
        .let { shareIds ->
            items.filter { item -> item.item.shareId in shareIds }
        }

}
