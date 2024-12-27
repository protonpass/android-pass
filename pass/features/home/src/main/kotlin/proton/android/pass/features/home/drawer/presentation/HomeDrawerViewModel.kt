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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.shares.ObserveSharesItemsCount
import proton.android.pass.domain.Share
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.VaultSelectionOption
import javax.inject.Inject

@HiltViewModel
class HomeDrawerViewModel @Inject constructor(
    canCreateVault: CanCreateVault,
    observeAllShares: ObserveAllShares,
    observeSharesItemsCount: ObserveSharesItemsCount,
    observeItemCount: ObserveItemCount,
    private val homeSearchOptionsRepository: HomeSearchOptionsRepository
) : ViewModel() {

    private val vaultSharesFlow = observeAllShares()
        .mapLatest { shares ->
            shares
                .filterIsInstance<Share.Vault>()
                .sortedBy { vaultShare -> vaultShare.name.lowercase() }
        }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            replay = 1
        )

    private val vaultSharesItemsCountFlow = vaultSharesFlow
        .flatMapLatest { vaultShares ->
            vaultShares
                .map { vaultShare -> vaultShare.id }
                .let { vaultShareIds -> observeSharesItemsCount(vaultShareIds) }
        }

    private val itemCountSummaryOptionFlow = observeItemCount()
        .mapLatest(::Some)

    internal val stateFlow: StateFlow<HomeDrawerState> = combine(
        vaultSharesFlow,
        vaultSharesItemsCountFlow,
        canCreateVault(),
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
