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

package proton.android.pass.featuresharing.impl.accept

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.featuresharing.impl.SharingSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.PendingInvite
import javax.inject.Inject

@HiltViewModel
class AcceptInviteViewModel @Inject constructor(
    private val acceptInvite: AcceptInvite,
    private val rejectInvite: RejectInvite,
    private val snackbarDispatcher: SnackbarDispatcher,
    observeInvites: ObserveInvites
) : ViewModel() {

    private val loadingFlow: MutableStateFlow<LoadingButtonState> =
        MutableStateFlow(LoadingButtonState.NotLoading)

    private val eventFlow: MutableStateFlow<AcceptInviteEvent> =
        MutableStateFlow(AcceptInviteEvent.Unknown)

    private val inviteFlow: Flow<LoadingResult<PendingInvite?>> = observeInvites()
        .asLoadingResult()
        .map {
            it.map { invites ->
                invites.firstOrNull()
            }
        }
        .distinctUntilChanged()


    val state: StateFlow<AcceptInviteUiState> = combine(
        loadingFlow,
        inviteFlow,
        eventFlow
    ) { isLoading, invite, event ->
        when (invite) {
            is LoadingResult.Error -> {
                PassLogger.w(TAG, invite.exception, "Error loading invite")
                AcceptInviteUiState.Error
            }
            LoadingResult.Loading -> AcceptInviteUiState.Loading
            is LoadingResult.Success -> {
                val inviteData = invite.data
                if (inviteData == null) {
                    PassLogger.w(TAG, "There is no pending invite")
                    AcceptInviteUiState.Error
                } else {
                    AcceptInviteUiState.Content(
                        invite = inviteData,
                        isConfirmLoading = isLoading == LoadingButtonState.ConfirmLoading,
                        isRejectLoading = isLoading == LoadingButtonState.RejectLoading,
                        areButtonsEnabled = isLoading == LoadingButtonState.NotLoading,
                        event = event
                    )
                }
            }
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AcceptInviteUiState.Loading
    )

    fun onConfirm(invite: PendingInvite) = viewModelScope.launch {
        loadingFlow.update { LoadingButtonState.ConfirmLoading }
        runCatching { acceptInvite(invite.inviteToken) }
            .onSuccess {
                PassLogger.i(TAG, "Invite accepted")
                eventFlow.update { AcceptInviteEvent.Close }
                snackbarDispatcher(SharingSnackbarMessage.InviteAccepted)
            }
            .onFailure {
                PassLogger.w(TAG, it, "Error accepting invite")
                snackbarDispatcher(SharingSnackbarMessage.InviteAcceptError)
            }
        loadingFlow.update { LoadingButtonState.NotLoading }
    }

    fun onReject(invite: PendingInvite) = viewModelScope.launch {
        loadingFlow.update { LoadingButtonState.RejectLoading }
        runCatching { rejectInvite(invite.inviteToken) }
            .onSuccess {
                PassLogger.i(TAG, "Invite rejected")
                eventFlow.update { AcceptInviteEvent.Close }
                snackbarDispatcher(SharingSnackbarMessage.InviteRejected)
            }
            .onFailure {
                PassLogger.w(TAG, it, "Error rejected invite")
                eventFlow.update { AcceptInviteEvent.Close }
                snackbarDispatcher(SharingSnackbarMessage.InviteRejectError)
            }
        loadingFlow.update { LoadingButtonState.NotLoading }
    }

    sealed interface LoadingButtonState {
        object ConfirmLoading : LoadingButtonState
        object RejectLoading : LoadingButtonState
        object NotLoading : LoadingButtonState
    }

    companion object {
        private const val TAG = "AcceptInviteViewModel"
    }
}
