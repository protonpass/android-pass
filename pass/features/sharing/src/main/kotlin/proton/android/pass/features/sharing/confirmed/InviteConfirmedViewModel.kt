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

package proton.android.pass.features.sharing.confirmed

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
import proton.android.pass.domain.PendingInvite
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class InviteConfirmedViewModel @Inject constructor(
    private val acceptInvite: AcceptInvite,
    private val rejectInvite: RejectInvite,
    private val snackbarDispatcher: SnackbarDispatcher,
    observeInvites: ObserveInvites
) : ViewModel() {

    private val buttonsFlow: MutableStateFlow<InviteConfirmedButtonsState> =
        MutableStateFlow(InviteConfirmedButtonsState.Initial)

    private val progressFlow: MutableStateFlow<InviteConfirmedProgressState> =
        MutableStateFlow(InviteConfirmedProgressState.Hide)

    private val eventFlow: MutableStateFlow<InviteConfirmedEvent> =
        MutableStateFlow(InviteConfirmedEvent.Unknown)

    private val inviteFlow: Flow<LoadingResult<PendingInvite?>> = observeInvites()
        .map { invites -> invites.firstOrNull { invite -> invite.isFromNewUser } }
        .take(1) // So when we accept the invite it doesn't re-emit
        .asLoadingResult()
        .onEach {
            if (it is LoadingResult.Error) {
                PassLogger.w(TAG, "Error loading invite")
                PassLogger.w(TAG, it.exception)
                eventFlow.update { InviteConfirmedEvent.Close }
            }
        }
        .distinctUntilChanged()

    val state: StateFlow<InviteConfirmedUiState> = combine(
        buttonsFlow,
        inviteFlow,
        progressFlow,
        eventFlow
    ) { buttons, invite, progress, event ->
        val content = when (invite) {
            LoadingResult.Loading -> InviteConfirmedUiContent.Loading
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error loading invite")
                PassLogger.w(TAG, invite.exception)
                snackbarDispatcher(SharingSnackbarMessage.GetInviteError)
                InviteConfirmedUiContent.Loading
            }

            is LoadingResult.Success -> InviteConfirmedUiContent.Content(
                invite = invite.data,
                buttonsState = buttons,
                progressState = progress
            )
        }
        InviteConfirmedUiState(
            event = event,
            content = content
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = InviteConfirmedUiState.Initial
    )

    @Suppress("LongMethod")
    fun onConfirm(invite: PendingInvite?) = viewModelScope.launch {
        if (invite == null) return@launch
        acceptInvite(invite.inviteToken)
            .catch {
                PassLogger.w(TAG, "Error accepting invite")
                PassLogger.w(TAG, it)
                snackbarDispatcher(SharingSnackbarMessage.InviteAcceptError)
                buttonsFlow.update {
                    InviteConfirmedButtonsState(
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
                            InviteConfirmedButtonsState(
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
                            InviteConfirmedButtonsState(
                                confirmLoading = true,
                                rejectLoading = false,
                                hideReject = true,
                                enabled = false
                            )
                        }
                        progressFlow.update {
                            InviteConfirmedProgressState.Show(
                                downloaded = status.downloaded,
                                total = status.total
                            )
                        }
                    }

                    is AcceptInviteStatus.Done -> {
                        PassLogger.d(TAG, "Items downloaded")

                        buttonsFlow.update {
                            InviteConfirmedButtonsState(
                                confirmLoading = true,
                                rejectLoading = false,
                                hideReject = true,
                                enabled = false
                            )
                        }
                        progressFlow.update {
                            InviteConfirmedProgressState.Show(
                                downloaded = status.items,
                                total = status.items
                            )
                        }
                        eventFlow.update { InviteConfirmedEvent.Confirmed(status.shareId) }
                        snackbarDispatcher(SharingSnackbarMessage.InviteAccepted)
                    }
                }
            }
    }

    fun onReject(invite: PendingInvite?) = viewModelScope.launch {
        if (invite == null) return@launch

        buttonsFlow.update {
            InviteConfirmedButtonsState(
                confirmLoading = false,
                rejectLoading = true,
                enabled = false,
                hideReject = false
            )
        }

        runCatching { rejectInvite(invite.inviteToken) }
            .onSuccess {
                PassLogger.i(TAG, "Invite rejected")
                eventFlow.update { InviteConfirmedEvent.Close }
                snackbarDispatcher(SharingSnackbarMessage.InviteRejected)
            }
            .onFailure {
                PassLogger.w(TAG, "Error rejected invite")
                PassLogger.w(TAG, it)
                eventFlow.update { InviteConfirmedEvent.Close }
                snackbarDispatcher(SharingSnackbarMessage.InviteRejectError)
            }

        buttonsFlow.update {
            InviteConfirmedButtonsState(
                confirmLoading = false,
                rejectLoading = false,
                enabled = false,
                hideReject = false
            )
        }
    }

    fun clearEvent() {
        eventFlow.update { InviteConfirmedEvent.Unknown }
    }

    companion object {
        private const val TAG = "InviteConfirmedViewModel"
    }

}
