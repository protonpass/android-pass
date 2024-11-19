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

package proton.android.pass.features.sharing.accept

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class AcceptInviteViewModel @Inject constructor(
    private val acceptInvite: AcceptInvite,
    private val rejectInvite: RejectInvite,
    private val snackbarDispatcher: SnackbarDispatcher,
    observeInvites: ObserveInvites
) : ViewModel() {

    private val buttonsFlow: MutableStateFlow<AcceptInviteButtonsState> =
        MutableStateFlow(AcceptInviteButtonsState.Initial)

    private val progressFlow: MutableStateFlow<AcceptInviteProgressState> =
        MutableStateFlow(AcceptInviteProgressState.Hide)

    private val eventFlow: MutableStateFlow<AcceptInviteEvent> = MutableStateFlow(
        value = AcceptInviteEvent.Idle
    )

    private val pendingInviteFlow: Flow<PendingInvite?> = oneShot {
        observeInvites()
            .first()
            .firstOrNull()
    }

    internal val stateFlow: StateFlow<AcceptInviteState> = combine(
        eventFlow,
        pendingInviteFlow
    ) { event, pendingInvite ->
        when (pendingInvite) {
            is PendingInvite.Item -> {
                AcceptInviteState.ItemInvite(
                    event = event,
                    pendingItemInvite = pendingInvite
                )
            }

            is PendingInvite.Vault -> {
                AcceptInviteState.VaultInvite(
                    event = event,
                    pendingVaultInvite = pendingInvite
                )
            }

            else -> {
                AcceptInviteState.Initial
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AcceptInviteState.Initial
    )

    internal fun onConsumeEvent(event: AcceptInviteEvent) {
        eventFlow.compareAndSet(event, AcceptInviteEvent.Idle)
    }

    internal fun onAcceptInvite(inviteToken: InviteToken) {
        viewModelScope.launch {

        }
    }

    internal fun onRejectInvite(inviteToken: InviteToken) {
        viewModelScope.launch {
            runCatching { rejectInvite(inviteToken) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error rejecting invite")
                    PassLogger.w(TAG, error)
                    eventFlow.update { AcceptInviteEvent.Close }
                    snackbarDispatcher(SharingSnackbarMessage.InviteRejectError)
                }
                .onSuccess {
                    PassLogger.i(TAG, "Invite successfully rejected")
                    eventFlow.update { AcceptInviteEvent.Close }
                    snackbarDispatcher(SharingSnackbarMessage.InviteRejected)
                }
        }
    }

    @Suppress("LongMethod")
    fun onConfirm(invite: PendingInvite?) = viewModelScope.launch {
        if (invite == null) return@launch
        acceptInvite(invite.inviteToken)
            .catch {
                PassLogger.w(TAG, "Error accepting invite")
                PassLogger.w(TAG, it)

                val message = if (it is CannotCreateMoreVaultsError) {
                    SharingSnackbarMessage.InviteAcceptErrorCannotCreateMoreVaults
                } else {
                    SharingSnackbarMessage.InviteAcceptError
                }
                snackbarDispatcher(message)

                buttonsFlow.update {
                    AcceptInviteButtonsState(
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
                            AcceptInviteButtonsState(
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
                            AcceptInviteButtonsState(
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
                            AcceptInviteButtonsState(
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
            AcceptInviteButtonsState(
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
                PassLogger.w(TAG, "Error rejected invite")
                PassLogger.w(TAG, it)
                eventFlow.update { AcceptInviteEvent.Close }
                snackbarDispatcher(SharingSnackbarMessage.InviteRejectError)
            }

        buttonsFlow.update {
            AcceptInviteButtonsState(
                confirmLoading = false,
                rejectLoading = false,
                enabled = false,
                hideReject = false
            )
        }
    }

    private companion object {

        private const val TAG = "AcceptInviteViewModel"

    }

}
