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

package proton.android.pass.features.vault.organise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.BatchChangeShareVisibility
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByVisibility
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class OrganiseVaultsViewModel @Inject constructor(
    observeVaultsGroupedByVisibility: ObserveVaultsGroupedByVisibility,
    private val batchChangeShareVisibility: BatchChangeShareVisibility,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val pendingVisibilityChangesStateFlow: MutableStateFlow<Map<ShareId, Boolean>> =
        MutableStateFlow(emptyMap())
    private val eventsStateFlow: MutableStateFlow<OrganiseVaultsEvent> =
        MutableStateFlow(OrganiseVaultsEvent.Idle)
    private val isSubmitLoadingStateFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    val state = combine(
        observeVaultsGroupedByVisibility(),
        pendingVisibilityChangesStateFlow,
        isSubmitLoadingStateFlow,
        eventsStateFlow
    ) { (hidden, visible), pending, isSubmitLoading, event ->
        val allVaults = hidden + visible

        val (finalHidden, finalVisible) = allVaults.partition { vault ->
            when (pending[vault.vault.shareId]) {
                true -> false
                false -> true
                null -> vault in hidden
            }
        }

        OrganiseVaultsUIState(
            hiddenVaults = finalHidden,
            visibleVaults = finalVisible,
            areTherePendingChanges = pending.isNotEmpty(),
            isSubmitLoading = isSubmitLoading,
            event = event
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = OrganiseVaultsUIState.Initial
        )

    fun onConfirm() {
        viewModelScope.launch {
            isSubmitLoadingStateFlow.update { IsLoadingState.Loading }
            runCatching {
                batchChangeShareVisibility(pendingVisibilityChangesStateFlow.value)
            }
                .onSuccess {
                    PassLogger.i(TAG, "Successfully changed vaults visibility")
                    snackbarDispatcher(OrganiseVaultsSnackbarMessage.OrganiseVaultsSuccess)
                    eventsStateFlow.update { OrganiseVaultsEvent.Close }
                }
                .onFailure {
                    PassLogger.w(TAG, it)
                    PassLogger.w(TAG, "Failed to change vaults visibility")
                    snackbarDispatcher(OrganiseVaultsSnackbarMessage.OrganiseVaultsError)
                }
            isSubmitLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    fun onVisibilityChange(shareId: ShareId, selected: Boolean) {
        pendingVisibilityChangesStateFlow.update {
            it + (shareId to selected)
        }
    }

    internal fun onConsumeEvent(event: OrganiseVaultsEvent) {
        eventsStateFlow.compareAndSet(event, OrganiseVaultsEvent.Idle)
    }

    companion object {
        private const val TAG = "OrganiseVaultsViewModel"
    }
}
