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

package proton.android.pass.features.vault.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.data.api.usecases.LeaveShare
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class LeaveVaultViewModel @Inject constructor(
    private val leaveShare: LeaveShare,
    private val snackbarDispatcher: SnackbarDispatcher,
    getVaultByShareId: GetVaultByShareId,
    savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private val shareId = ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))

    private val eventFlow: MutableStateFlow<LeaveVaultEvent> =
        MutableStateFlow(LeaveVaultEvent.Unknown)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val vaultState: Flow<LoadingResult<Vault>> = getVaultByShareId(shareId = shareId)
        .asLoadingResult()
        .distinctUntilChanged()

    val state: StateFlow<LeaveVaultUiState> = combine(
        vaultState,
        eventFlow,
        isLoadingState
    ) { vaultResult, event, isLoadingState ->
        val (vaultName, loading) = when (vaultResult) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error getting vault by id")
                PassLogger.w(TAG, vaultResult.exception)
                snackbarDispatcher(VaultSnackbarMessage.CannotRetrieveVaultError)
                eventFlow.update { LeaveVaultEvent.Close }
                "" to isLoadingState
            }
            LoadingResult.Loading -> "" to IsLoadingState.Loading
            is LoadingResult.Success -> vaultResult.data.name to isLoadingState
        }

        LeaveVaultUiState(
            event = event,
            isLoadingState = loading,
            vaultName = vaultName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LeaveVaultUiState.Initial
    )

    fun onLeave() = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { leaveShare.invoke(shareId) }
            .onSuccess {
                snackbarDispatcher(VaultSnackbarMessage.LeaveVaultSuccess)
                eventFlow.update { LeaveVaultEvent.Left }
            }
            .onFailure {
                PassLogger.w(TAG, "Error leaving vault")
                PassLogger.w(TAG, it)
                snackbarDispatcher(VaultSnackbarMessage.LeaveVaultError)
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun clearEvent() {
        eventFlow.update { LeaveVaultEvent.Unknown }
    }

    companion object {
        private const val TAG = "LeaveVaultViewModel"
    }

}
