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

package proton.android.pass.featuresettings.impl.primaryvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.MarkVaultAsPrimary
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featuresettings.impl.SettingsSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.VaultWithItemCount
import javax.inject.Inject

@HiltViewModel
class SelectPrimaryVaultViewModel @Inject constructor(
    private val markVaultAsPrimary: MarkVaultAsPrimary,
    private val snackbarDispatcher: SnackbarDispatcher,
    observeVaults: ObserveVaultsWithItemCount
) : ViewModel() {

    private val eventFlow: MutableStateFlow<SelectPrimaryVaultEvent> =
        MutableStateFlow(SelectPrimaryVaultEvent.Unknown)
    private val loadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val vaultsFlow: Flow<ImmutableList<VaultWithItemCount>> = observeVaults()
        .asLoadingResult()
        .map {
            when (it) {
                LoadingResult.Loading -> persistentListOf()
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, it.exception, "Error observing vaults")
                    persistentListOf()
                }
                is LoadingResult.Success -> it.data.toImmutableList()
            }
        }

    val state: StateFlow<SelectPrimaryVaultUiState> = combine(
        vaultsFlow,
        eventFlow,
        loadingFlow
    ) { vaults, event, loading ->
        SelectPrimaryVaultUiState(
            vaults = vaults,
            event = event,
            loading = loading
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectPrimaryVaultUiState.Initial
    )

    fun onVaultSelected(vault: VaultWithItemCount) = viewModelScope.launch {
        loadingFlow.update { IsLoadingState.Loading }
        runCatching {
            markVaultAsPrimary(shareId = vault.vault.shareId)
        }.onSuccess {
            eventFlow.update { SelectPrimaryVaultEvent.Selected }
            snackbarDispatcher(SettingsSnackbarMessage.ChangePrimaryVaultSuccess)
        }.onFailure {
            PassLogger.e(TAG, it, "Error marking vault as primary")
            snackbarDispatcher(SettingsSnackbarMessage.ChangePrimaryVaultError)
        }
        loadingFlow.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "SelectPrimaryVaultViewModel"
    }
}
