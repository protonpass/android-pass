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

package proton.android.pass.features.sharing.manage.iteminviteoptions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.errors.CannotSendMoreInvitesError
import proton.android.pass.data.api.usecases.CancelInvite
import proton.android.pass.data.api.usecases.ResendShareInvite
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.features.sharing.manage.bottomsheet.InviteIdArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ManageItemInviteOptionsViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val resendShareInvite: ResendShareInvite,
    private val cancelInvite: CancelInvite,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val inviteId: InviteId = savedStateHandleProvider.get()
        .require<String>(InviteIdArg.key)
        .let(::InviteId)

    private val eventFlow = MutableStateFlow<ManageItemInviteOptionsEvent>(
        value = ManageItemInviteOptionsEvent.Idle
    )

    private val actionFlow = MutableStateFlow<ManageItemInviteOptionsAction>(
        value = ManageItemInviteOptionsAction.None
    )

    internal val stateFlow: StateFlow<ManageItemInviteOptionsState> = combine(
        eventFlow,
        actionFlow,
        ::ManageItemInviteOptionsState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = ManageItemInviteOptionsState.Initial
    )

    internal fun onConsumeEvent(event: ManageItemInviteOptionsEvent) {
        eventFlow.compareAndSet(event, ManageItemInviteOptionsEvent.Idle)
    }

    internal fun onResendInvite() {
        viewModelScope.launch {
            actionFlow.update { ManageItemInviteOptionsAction.ResendInvite }

            runCatching { resendShareInvite(shareId, inviteId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error re-sending the invite")
                    PassLogger.w(TAG, error)

                    eventFlow.update { ManageItemInviteOptionsEvent.OnResendInviteFailure }
                    if (error is CannotSendMoreInvitesError) {
                        SharingSnackbarMessage.TooManyInvitesSentError
                    } else {
                        SharingSnackbarMessage.ResendInviteError
                    }.also { message -> snackbarDispatcher(message) }
                }
                .onSuccess {
                    eventFlow.update { ManageItemInviteOptionsEvent.OnResendInviteSuccess }
                    snackbarDispatcher(SharingSnackbarMessage.ResendInviteSuccess)
                }

            actionFlow.update { ManageItemInviteOptionsAction.None }
        }
    }

    internal fun onCancelInvite() {
        viewModelScope.launch {
            actionFlow.update { ManageItemInviteOptionsAction.CancelInvite }

            runCatching { cancelInvite(shareId, inviteId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error canceling the invite")
                    PassLogger.w(TAG, error)

                    eventFlow.update { ManageItemInviteOptionsEvent.OnCancelInviteFailure }
                    snackbarDispatcher(SharingSnackbarMessage.CancelInviteError)
                }
                .onSuccess {
                    eventFlow.update { ManageItemInviteOptionsEvent.OnCancelInviteSuccess }
                    snackbarDispatcher(SharingSnackbarMessage.CancelInviteSuccess)
                }

            actionFlow.update { ManageItemInviteOptionsAction.None }
        }
    }

    private companion object {

        private const val TAG = "ManageItemInviteOptionsViewModel"

    }

}
