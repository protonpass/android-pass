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

package proton.android.pass.features.sharing.manage.bottomsheet.inviteoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.errors.CannotSendMoreInvitesError
import proton.android.pass.data.api.usecases.CancelInvite
import proton.android.pass.data.api.usecases.ResendShareInvite
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.features.sharing.manage.bottomsheet.InviteIdArg
import proton.android.pass.features.sharing.manage.bottomsheet.InviteTypeArg
import proton.android.pass.features.sharing.manage.bottomsheet.InviteTypeValue
import proton.android.pass.features.sharing.manage.bottomsheet.InviteTypeValue.Companion.INVITE_TYPE_EXISTING_USER
import proton.android.pass.features.sharing.manage.bottomsheet.InviteTypeValue.Companion.INVITE_TYPE_NEW_USER
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class InviteOptionsViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val resendShareInvite: ResendShareInvite,
    private val cancelInvite: CancelInvite,
    savedState: SavedStateHandleProvider
) : ViewModel() {

    private val shareId = ShareId(savedState.get().require(CommonNavArgId.ShareId.key))
    private val inviteType: InviteTypeValue = run {
        val inviteId: String = savedState.get().require(InviteIdArg.key)
        when (val type: String = savedState.get().require(InviteTypeArg.key)) {
            INVITE_TYPE_EXISTING_USER -> InviteTypeValue.ExistingUserInvite(InviteId(inviteId))
            INVITE_TYPE_NEW_USER -> InviteTypeValue.NewUserInvite(NewUserInviteId(inviteId))
            else -> throw IllegalArgumentException("Unknown invite type: $type")
        }
    }

    private val loadingOptionFlow: MutableStateFlow<LoadingOption?> = MutableStateFlow(null)
    private val eventFlow: MutableStateFlow<InviteOptionsEvent> =
        MutableStateFlow(InviteOptionsEvent.Unknown)

    val state: StateFlow<InviteOptionsUiState> = combine(
        loadingOptionFlow,
        flowOf(showResendInvite()),
        eventFlow,
        ::InviteOptionsUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = InviteOptionsUiState.Initial(showResendInvite())
    )

    fun cancelInvite() = viewModelScope.launch {
        loadingOptionFlow.update { LoadingOption.CancelInvite }
        runCatching {
            when (inviteType) {
                is InviteTypeValue.ExistingUserInvite -> {
                    cancelInvite.invoke(shareId, inviteType.inviteId)
                }
                is InviteTypeValue.NewUserInvite -> {
                    cancelInvite.invoke(shareId, inviteType.inviteId)
                }
            }

        }.onSuccess {
            PassLogger.i(TAG, "Invite canceled")
            eventFlow.update { InviteOptionsEvent.Close(refresh = true) }
            snackbarDispatcher(SharingSnackbarMessage.CancelInviteSuccess)
        }.onFailure {
            PassLogger.w(TAG, "Error canceling invite")
            PassLogger.w(TAG, it)
            snackbarDispatcher(SharingSnackbarMessage.CancelInviteError)
        }

        loadingOptionFlow.update { null }
    }

    fun resendInvite() = viewModelScope.launch {
        val inviteId = when (inviteType) {
            is InviteTypeValue.ExistingUserInvite -> inviteType.inviteId
            is InviteTypeValue.NewUserInvite -> return@launch
        }

        PassLogger.i(TAG, "Resending invite: $inviteId")
        loadingOptionFlow.update { LoadingOption.ResendInvite }
        runCatching {
            resendShareInvite.invoke(shareId, inviteId)
        }.onSuccess {
            PassLogger.i(TAG, "Invite resent")
            eventFlow.update { InviteOptionsEvent.Close(refresh = true) }
            snackbarDispatcher(SharingSnackbarMessage.ResendInviteSuccess)
        }.onFailure {
            PassLogger.w(TAG, "Error resending invite")
            PassLogger.w(TAG, it)
            val message = if (it is CannotSendMoreInvitesError) {
                SharingSnackbarMessage.TooManyInvitesSentError
            } else {
                SharingSnackbarMessage.ResendInviteError
            }
            snackbarDispatcher(message)
        }
        loadingOptionFlow.update { null }
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.update { InviteOptionsEvent.Unknown }
    }

    private fun showResendInvite(): Boolean = inviteType is InviteTypeValue.ExistingUserInvite

    companion object {
        private const val TAG = "InviteOptionsViewModel"
    }
}
