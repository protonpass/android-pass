/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.searchoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.items.ObserveSharedItemCountSummary
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.items.ItemSharedType
import proton.android.pass.searchoptions.api.FilterOption
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.VaultSelectionOption
import javax.inject.Inject

@HiltViewModel
class FilterBottomSheetViewModel @Inject constructor(
    observeItemCount: ObserveItemCount,
    observeSharedItemCountSummary: ObserveSharedItemCountSummary,
    private val homeSearchOptionsRepository: HomeSearchOptionsRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val summaryAndOptionsFlow = homeSearchOptionsRepository.observeSearchOptions()
        .flatMapLatest {
            when (val vault = it.vaultSelectionOption) {
                VaultSelectionOption.AllVaults -> observeItemCount(
                    shareSelection = ShareSelection.AllShares,
                    includeHiddenVault = false
                )

                VaultSelectionOption.SharedByMe -> observeSharedItemCountSummary(
                    itemSharedType = ItemSharedType.SharedByMe,
                    includeHiddenVault = false
                )

                VaultSelectionOption.SharedWithMe -> observeSharedItemCountSummary(
                    itemSharedType = ItemSharedType.SharedWithMe,
                    includeHiddenVault = false
                )

                VaultSelectionOption.Trash -> observeItemCount(
                    itemState = ItemState.Trashed,
                    shareSelection = ShareSelection.AllShares,
                    includeHiddenVault = false
                )

                is VaultSelectionOption.Vault -> observeItemCount(
                    shareSelection = ShareSelection.Share(vault.shareId),
                    includeHiddenVault = false
                )
            }.zip(flowOf(it)) { itemCount, searchOptions ->
                itemCount to searchOptions
            }
        }

    internal val stateFlow: StateFlow<FilterOptionsState> =
        summaryAndOptionsFlow.map { (summary, options) ->
            FilterOptionsState.Success(
                searchOptions = options,
                summary = summary
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FilterOptionsState.Empty
        )

    internal fun onFilterTypeChanged(searchFilterType: SearchFilterType) {
        FilterOption(searchFilterType).also(homeSearchOptionsRepository::setFilterOption)
    }

}
