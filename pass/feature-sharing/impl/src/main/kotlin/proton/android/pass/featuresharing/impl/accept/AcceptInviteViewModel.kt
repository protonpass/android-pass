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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.AcceptInviteStatus
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
    observeInvites: ObserveInvites,

) : ViewModel() {

    private val buttonsFlow: MutableStateFlow<AcceptInviteButtonsState> =
        MutableStateFlow(AcceptInviteButtonsState.Show.Initial)

    private val progressFlow: MutableStateFlow<AcceptInviteProgressState> =
        MutableStateFlow(AcceptInviteProgressState.Hide)

    private val eventFlow: MutableStateFlow<AcceptInviteEvent> =
        MutableStateFlow(AcceptInviteEvent.Unknown)

    private val inviteFlow: Flow<LoadingResult<PendingInvite?>> = observeInvites()
        .map { invites -> invites.firstOrNull() }
        .take(1) // So when we accept the invite it doesn't re-emit
        .asLoadingResult()
        .onEach {
            if (it is LoadingResult.Error) {
                PassLogger.w(TAG, it.exception, "Error loading invite")
                eventFlow.update { AcceptInviteEvent.Close }
            }
        }
        .distinctUntilChanged()

    val state: StateFlow<AcceptInviteUiState> = combine(
        buttonsFlow,
        inviteFlow,
        progressFlow,
        eventFlow
    ) { buttons, invite, progress, event ->
        val content = when (invite) {
            LoadingResult.Loading -> AcceptInviteUiContent.Loading
            is LoadingResult.Error -> {
                PassLogger.w(TAG, invite.exception, "Error loading invite")
                snackbarDispatcher(SharingSnackbarMessage.GetInviteError)
                AcceptInviteUiContent.Loading
            }

            is LoadingResult.Success -> AcceptInviteUiContent.Content(
                invite = invite.data,
                buttonsState = buttons,
                progressState = progress
            )
        }
        AcceptInviteUiState(
            event = event,
            content = content
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AcceptInviteUiState.Initial
    )

    @Suppress("LongMethod")
    fun onConfirm(invite: PendingInvite?) = viewModelScope.launch {
        if (invite == null) return@launch
        acceptInvite(invite.inviteToken)
            .catch {
                PassLogger.w(TAG, it, "Error accepting invite")
                snackbarDispatcher(SharingSnackbarMessage.InviteAcceptError)
                buttonsFlow.update {
                    AcceptInviteButtonsState.Show(
                        confirmLoading = false,
                        rejectLoading = false,
                        hideReject = false,
                        enabled = true
                    )
                }
            }
            .collect { status ->
                when (status) {
                    AcceptInviteStatus.AcceptingInvite -> {
                        buttonsFlow.update {
                            AcceptInviteButtonsState.Show(
                                confirmLoading = true,
                                rejectLoading = false,
                                hideReject = true,
                                enabled = false
                            )
                        }
                    }

                    is AcceptInviteStatus.DownloadingItems -> {
                        PassLogger.d(TAG, "Downloading items")

                        buttonsFlow.update {
                            AcceptInviteButtonsState.Show(
                                confirmLoading = true,
                                rejectLoading = false,
                                hideReject = true,
                                enabled = false
                            )
                        }
                        progressFlow.update {
                            AcceptInviteProgressState.Show(
                                downloaded = status.downloaded,
                                total = status.total
                            )
                        }
                    }

                    is AcceptInviteStatus.Done -> {
                        PassLogger.d(TAG, "Items downloaded")

                        buttonsFlow.update {
                            AcceptInviteButtonsState.Show(
                                confirmLoading = true,
                                rejectLoading = false,
                                hideReject = true,
                                enabled = false
                            )
                        }
                        progressFlow.update {
                            AcceptInviteProgressState.Show(
                                downloaded = status.items,
                                total = status.items
                            )
                        }
                        eventFlow.update { AcceptInviteEvent.Close }
                        snackbarDispatcher(SharingSnackbarMessage.InviteAccepted)
                    }
                }
            }
    }

    fun onReject(invite: PendingInvite?) = viewModelScope.launch {
        if (invite == null) return@launch

        buttonsFlow.update {
            AcceptInviteButtonsState.Show(
                confirmLoading = false,
                rejectLoading = true,
                enabled = false,
                hideReject = false
            )
        }

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

        buttonsFlow.update {
            AcceptInviteButtonsState.Show(
                confirmLoading = false,
                rejectLoading = false,
                enabled = false,
                hideReject = false
            )
        }
    }

    fun clearEvent() {
        eventFlow.update { AcceptInviteEvent.Unknown }
    }

    companion object {
        private const val TAG = "AcceptInviteViewModel"
    }
}
