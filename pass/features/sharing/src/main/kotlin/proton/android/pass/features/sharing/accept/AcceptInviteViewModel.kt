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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.data.api.usecases.invites.ObserveInvite
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class AcceptInviteViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val acceptInvite: AcceptInvite,
    private val rejectInvite: RejectInvite,
    private val snackbarDispatcher: SnackbarDispatcher,
    observeInvite: ObserveInvite
) : ViewModel() {

    private val inviteToken = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.InviteToken.key)
        .let(::InviteToken)

    private val progressFlow: MutableStateFlow<AcceptInviteProgress> = MutableStateFlow(
        value = AcceptInviteProgress.Pending
    )

    private val eventFlow: MutableStateFlow<AcceptInviteEvent> = MutableStateFlow(
        value = AcceptInviteEvent.Idle
    )

    private val pendingInviteOptionFlow: Flow<Option<PendingInvite>> = oneShot {
        observeInvite(inviteToken).first()
    }

    internal val stateFlow: StateFlow<AcceptInviteState> = combine(
        pendingInviteOptionFlow,
        progressFlow,
        eventFlow
    ) { pendingInviteOption, progress, event ->
        when (pendingInviteOption) {
            None -> AcceptInviteState.Initial
            is Some -> when (val pendingInvite = pendingInviteOption.value) {
                is PendingInvite.Item -> AcceptInviteState.ItemInvite(
                    progress = progress,
                    event = event,
                    pendingItemInvite = pendingInvite
                )

                is PendingInvite.Vault -> AcceptInviteState.VaultInvite(
                    progress = progress,
                    event = event,
                    pendingVaultInvite = pendingInvite
                )
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

    internal fun onAcceptInvite() {
        viewModelScope.launch {
            acceptInvite(inviteToken)
                .catch { error ->
                    PassLogger.w(TAG, "There was an error accepting invite")
                    PassLogger.w(TAG, error)
                    eventFlow.update { AcceptInviteEvent.Close }

                    if (error is CannotCreateMoreVaultsError) {
                        SharingSnackbarMessage.InviteAcceptErrorCannotCreateMoreVaults
                    } else {
                        SharingSnackbarMessage.InviteAcceptError
                    }.also { errorMessage ->
                        snackbarDispatcher(errorMessage)
                    }
                }
                .collect { acceptInviteStatus ->
                    when (acceptInviteStatus) {
                        AcceptInviteStatus.AcceptingInvite -> {
                            PassLogger.d(TAG, "Accepting invite")
                            progressFlow.update { AcceptInviteProgress.Accepting }
                        }

                        is AcceptInviteStatus.DownloadingItems -> {
                            PassLogger.d(TAG, "Downloading invite items")
                            progressFlow.update {
                                AcceptInviteProgress.Downloading(
                                    downloaded = acceptInviteStatus.downloaded,
                                    total = acceptInviteStatus.total
                                )
                            }
                        }

                        is AcceptInviteStatus.Done -> {
                            PassLogger.i(TAG, "Invite successfully accepted")
                            eventFlow.update { AcceptInviteEvent.Close }
                            snackbarDispatcher(SharingSnackbarMessage.InviteAccepted)
                        }
                    }
                }
        }
    }

    internal fun onRejectInvite() {
        viewModelScope.launch {
            progressFlow.update { AcceptInviteProgress.Rejecting }

            runCatching { rejectInvite(inviteToken) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error rejecting invite")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(SharingSnackbarMessage.InviteRejectError)
                }
                .onSuccess {
                    PassLogger.i(TAG, "Invite successfully rejected")
                    snackbarDispatcher(SharingSnackbarMessage.InviteRejected)
                }

            eventFlow.update { AcceptInviteEvent.Close }
        }
    }

    private companion object {

        private const val TAG = "AcceptInviteViewModel"

    }

}
