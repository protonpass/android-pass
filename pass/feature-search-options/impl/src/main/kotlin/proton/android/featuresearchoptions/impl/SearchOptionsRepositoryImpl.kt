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

package proton.android.featuresearchoptions.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.featuresearchoptions.api.SearchOptions
import proton.android.pass.featuresearchoptions.api.SearchOptionsRepository
import proton.android.pass.featuresearchoptions.api.SortingOption
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchOptionsRepositoryImpl @Inject constructor() : SearchOptionsRepository {

    private val _searchOptionsState: MutableStateFlow<SearchOptions> =
        MutableStateFlow(SearchOptions.Initial)

    override fun observeSearchOptions(): Flow<SearchOptions> =
        _searchOptionsState

    override fun observeSortingOption(): Flow<SortingOption> =
        _searchOptionsState.map { it.sortingOption }.filterNotNull()

    override fun observeVaultSelectionOption(): Flow<VaultSelectionOption> =
        _searchOptionsState.map { it.vaultSelectionOption }.filterNotNull()


    override fun setSortingOption(sortingOption: SortingOption) {
        _searchOptionsState.update {
            it.copy(sortingOption = sortingOption)
        }
    }

    override fun setVaultSelectionOption(vaultSelectionOption: VaultSelectionOption) {
        _searchOptionsState.update {
            it.copy(vaultSelectionOption = vaultSelectionOption)
        }
    }

    override fun clearSearchOptions() {
        _searchOptionsState.update { SearchOptions.Initial }
    }
}
