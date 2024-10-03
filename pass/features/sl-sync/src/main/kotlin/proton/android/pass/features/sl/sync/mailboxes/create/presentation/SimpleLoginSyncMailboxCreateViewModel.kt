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

package proton.android.pass.features.sl.sync.mailboxes.create.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.simplelogin.CreateSimpleLoginAliasMailbox
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncMailboxCreateViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val emailValidator: EmailValidator,
    private val createSimpleLoginAliasMailbox: CreateSimpleLoginAliasMailbox,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    @OptIn(SavedStateHandleSaveableApi::class)
    private var mailboxEmailMutableState: String by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    private val showInvalidMailboxEmailErrorFlow = MutableStateFlow(false)

    private val eventFlow = MutableStateFlow<SimpleLoginSyncMailboxCreateEvent>(
        value = SimpleLoginSyncMailboxCreateEvent.Idle
    )

    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(
        value = IsLoadingState.NotLoading
    )

    internal val mailboxEmailState: String
        get() = mailboxEmailMutableState

    internal val stateFlow: StateFlow<SimpleLoginSyncMailboxCreateState> = combine(
        showInvalidMailboxEmailErrorFlow,
        eventFlow,
        isLoadingStateFlow,
        ::SimpleLoginSyncMailboxCreateState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SimpleLoginSyncMailboxCreateState.Initial
    )

    internal fun onConsumeEvent(event: SimpleLoginSyncMailboxCreateEvent) {
        eventFlow.compareAndSet(event, SimpleLoginSyncMailboxCreateEvent.Idle)
    }

    internal fun onCreateMailbox() {
        if (!emailValidator.isValid(mailboxEmailState)) {
            showInvalidMailboxEmailErrorFlow.update { true }
            return
        }

        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching { createSimpleLoginAliasMailbox(mailboxEmailState) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error creating the mailbox")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(SimpleLoginSyncMailboxCreateSnackbarMessage.CreateMailboxError)
                }
                .onSuccess {
                    eventFlow.update { SimpleLoginSyncMailboxCreateEvent.OnMailboxCreated }
                }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    internal fun onMailboxEmailChanged(newMailboxEmail: String) {
        showInvalidMailboxEmailErrorFlow.update { false }

        mailboxEmailMutableState = newMailboxEmail.trim()
    }

    private companion object {

        private const val TAG = "SimpleLoginSyncMailboxCreateViewModel"

    }

}
