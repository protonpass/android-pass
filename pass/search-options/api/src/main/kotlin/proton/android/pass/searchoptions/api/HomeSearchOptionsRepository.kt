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

package proton.android.pass.searchoptions.api

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ShareId

interface HomeSearchOptionsRepository {
    fun observeSearchOptions(): Flow<SearchOptions>
    fun observeSortingOption(): Flow<SortingOption>
    fun observeFilterOption(): Flow<FilterOption>
    fun observeVaultSelectionOption(): Flow<VaultSelectionOption>
    fun setSortingOption(sortingOption: SortingOption)
    fun setFilterOption(filterOption: FilterOption)
    suspend fun setVaultSelectionOption(vaultSelectionOption: VaultSelectionOption)
}

data class SearchOptions(
    val filterOption: FilterOption,
    val sortingOption: SortingOption,
    val vaultSelectionOption: VaultSelectionOption,
    val userId: UserId?
) {
    companion object {
        val Initial = SearchOptions(
            filterOption = FilterOption(SearchFilterType.All),
            sortingOption = SortingOption(SearchSortingType.MostRecent),
            vaultSelectionOption = VaultSelectionOption.AllVaults,
            userId = null
        )
    }
}


data class SortingOption(val searchSortingType: SearchSortingType)
data class FilterOption(val searchFilterType: SearchFilterType)

@Stable
sealed interface VaultSelectionOption {
    data object AllVaults : VaultSelectionOption
    data object Trash : VaultSelectionOption
    data class Vault(val shareId: ShareId) : VaultSelectionOption
}
