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

package proton.android.pass.featuresearchoptions.api

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import proton.pass.domain.ShareId

interface HomeSearchOptionsRepository {
    fun observeSearchOptions(): Flow<SearchOptions>
    fun observeSortingOption(): Flow<SortingOption>
    fun observeVaultSelectionOption(): Flow<VaultSelectionOption>
    fun setSortingOption(sortingOption: SortingOption)
    fun setVaultSelectionOption(vaultSelectionOption: VaultSelectionOption)
    fun clearSearchOptions()
}

data class SearchOptions(
    val sortingOption: SortingOption,
    val vaultSelectionOption: VaultSelectionOption
) {
    companion object {
        val Initial = SearchOptions(
            sortingOption = SortingOption(SearchSortingType.MostRecent),
            vaultSelectionOption = VaultSelectionOption.AllVaults
        )
    }
}


data class SortingOption(val searchSortingType: SearchSortingType)

@Stable
sealed class VaultSelectionOption {
    object AllVaults : VaultSelectionOption()
    object Trash : VaultSelectionOption()
    data class Vault(val shareId: ShareId) : VaultSelectionOption()
}
