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

package proton.android.pass.features.home.drawer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.capabilities.CanOrganiseVaults
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.VaultSelectionOption
import javax.inject.Inject

@HiltViewModel
class HomeDrawerViewModel @Inject constructor(
    canCreateVault: CanCreateVault,
    canOrganiseVaults: CanOrganiseVaults,
    observeVaultsWithItemCount: ObserveVaultsWithItemCount,
    observeItemCount: ObserveItemCount,
    private val homeSearchOptionsRepository: HomeSearchOptionsRepository
) : ViewModel() {

    private val vaultSharesItemsCountFlow: Flow<List<VaultWithItemCount>> =
        observeVaultsWithItemCount(includeHidden = false)
            .map { list -> list.sortedBy { it.vault.name.lowercase() } }

    private val itemCountSummaryOptionFlow: Flow<Some<ItemCountSummary>> =
        observeItemCount(applyItemStateToSharedItems = false, includeHiddenVault = false)
            .mapLatest(::Some)

    internal val stateFlow: StateFlow<HomeDrawerState> = combine(
        vaultSharesItemsCountFlow,
        canCreateVault(),
        canOrganiseVaults(),
        homeSearchOptionsRepository.observeVaultSelectionOption(),
        itemCountSummaryOptionFlow,
        ::HomeDrawerState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = HomeDrawerState.Initial
    )

    internal fun setVaultSelection(vaultSelectionOption: VaultSelectionOption) {
        viewModelScope.launch {
            homeSearchOptionsRepository.setVaultSelectionOption(vaultSelectionOption)
        }
    }

}
