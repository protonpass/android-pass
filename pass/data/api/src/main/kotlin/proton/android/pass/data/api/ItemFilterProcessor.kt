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

import proton.android.pass.domain.Item
import proton.android.pass.domain.Vault

data object ItemFilterProcessor {

    fun removeDuplicates(array: Array<Pair<List<Vault>, List<Item>>>): List<Item> {
        val distinctVaults = getDistinctVaults(array.map { it.first }.flatten())
        return filterItemsByVaults(array.map { it.second }.flatten(), distinctVaults)
    }

    private fun getDistinctVaults(vaultsList: List<Vault>): List<Vault> = vaultsList.groupBy { it.vaultId }
        .mapValues { (_, vaults) ->
            vaults.sortedWith(compareBy({ !it.isOwned }, Vault::role)).first()
        }
        .values
        .toList()

    private fun filterItemsByVaults(items: List<Item>, vaults: List<Vault>): List<Item> {
        val vaultShareIds = vaults.map { it.shareId }.toSet()
        return items.filter { it.shareId in vaultShareIds }
    }
}
