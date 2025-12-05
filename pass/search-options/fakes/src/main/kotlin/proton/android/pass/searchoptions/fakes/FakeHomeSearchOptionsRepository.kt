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

package proton.android.pass.searchoptions.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.searchoptions.api.FilterOption
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.SearchOptions
import proton.android.pass.searchoptions.api.SortingOption
import proton.android.pass.searchoptions.api.VaultSelectionOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeHomeSearchOptionsRepository @Inject constructor() : HomeSearchOptionsRepository {

    private val searchOptionsFlow = MutableStateFlow(SearchOptions.Initial)

    private val sortingOptionFlow = MutableStateFlow(SearchOptions.Initial.sortingOption)
    private val filterOptionFlow = MutableStateFlow(SearchOptions.Initial.filterOption)
    private val vaultSelectionOptionFlow: MutableStateFlow<VaultSelectionOption> =
        MutableStateFlow(VaultSelectionOption.AllVaults)

    override fun observeSearchOptions(): Flow<SearchOptions> = searchOptionsFlow

    override fun observeSortingOption(): Flow<SortingOption> = sortingOptionFlow

    override fun observeFilterOption(): Flow<FilterOption> = filterOptionFlow

    override fun observeVaultSelectionOption(): Flow<VaultSelectionOption> = vaultSelectionOptionFlow

    override fun setSortingOption(sortingOption: SortingOption) {
        sortingOptionFlow.update { sortingOption }
    }

    override fun setFilterOption(filterOption: FilterOption) {
        filterOptionFlow.update { filterOption }
    }

    override suspend fun setVaultSelectionOption(vaultSelectionOption: VaultSelectionOption) {
        vaultSelectionOptionFlow.update { vaultSelectionOption }
    }

}
