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

package proton.android.pass.features.sl.sync.mailboxes.options.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.simplelogin.CancelSimpleLoginAliasMailboxChange
import proton.android.pass.data.api.usecases.simplelogin.DeleteSimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.simplelogin.UpdateSimpleLoginAliasMailbox
import proton.android.pass.features.sl.sync.shared.navigation.mailboxes.SimpleLoginSyncMailboxIdNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncMailboxOptionsViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeSimpleLoginAliasMailbox: ObserveSimpleLoginAliasMailbox,
    private val updateSimpleLoginAliasMailbox: UpdateSimpleLoginAliasMailbox,
    private val deleteSimpleLoginAliasMailbox: DeleteSimpleLoginAliasMailbox,
    private val cancelSimpleLoginAliasMailboxChange: CancelSimpleLoginAliasMailboxChange,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val mailboxId = savedStateHandleProvider.get()
        .require<Long>(SimpleLoginSyncMailboxIdNavArgId.key)

    private val aliasMailboxOptionFlow =
        oneShot { observeSimpleLoginAliasMailbox(mailboxId).first() }
            .mapLatest { aliasMailbox ->
                if (aliasMailbox == null) {
                    throw IllegalStateException("Alias mailbox is null")
                }
                aliasMailbox.toOption()
            }
            .catch { error ->
                PassLogger.w(TAG, "There was an error observing alias mailbox")
                PassLogger.w(TAG, error)
                eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnMailboxOptionsError }
                snackbarDispatcher(SimpleLoginSyncMailboxOptionsMessage.MailboxOptionsError)
                emit(None)
            }

    private val eventFlow = MutableStateFlow<SimpleLoginSyncMailboxOptionsEvent>(
        value = SimpleLoginSyncMailboxOptionsEvent.Idle
    )

    private val actionFlow = MutableStateFlow(SimpleLoginSyncMailboxOptionsAction.None)

    internal val stateFlow: StateFlow<SimpleLoginSyncMailboxOptionsState> = combine(
        aliasMailboxOptionFlow,
        eventFlow,
        actionFlow
    ) { aliasMailboxOption, event, action ->
        SimpleLoginSyncMailboxOptionsState(
            aliasMailboxOption = aliasMailboxOption,
            event = event,
            action = action
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5_000L),
        initialValue = SimpleLoginSyncMailboxOptionsState.Initial
    )

    internal fun onSetMailboxAsDefault() {
        viewModelScope.launch {
            actionFlow.update { SimpleLoginSyncMailboxOptionsAction.SetAsDefault }

            runCatching { updateSimpleLoginAliasMailbox(mailboxId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error updating default mailbox")
                    PassLogger.w(TAG, error)
                    eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnMailboxSetAsDefaultError }
                    snackbarDispatcher(SimpleLoginSyncMailboxOptionsMessage.DefaultMailboxError)
                }
                .onSuccess {
                    eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnMailboxSetAsDefaultSuccess }
                    snackbarDispatcher(SimpleLoginSyncMailboxOptionsMessage.DefaultMailboxSuccess)
                }

            actionFlow.update { SimpleLoginSyncMailboxOptionsAction.None }
        }
    }

    internal fun onVerifyMailbox() {
        eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnVerifyMailbox(mailboxId) }
    }

    internal fun onChangeMailboxEmail() {
        eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnChangeMailboxEmail(mailboxId) }
    }

    internal fun onCancelMailboxEmailChange() {
        viewModelScope.launch {
            actionFlow.update { SimpleLoginSyncMailboxOptionsAction.CancelEmailChange }

            runCatching { cancelSimpleLoginAliasMailboxChange(mailboxId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error cancelling mailbox email change")
                    PassLogger.w(TAG, error)
                    eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnMailboxCancelEmailChangeError }
                    snackbarDispatcher(SimpleLoginSyncMailboxOptionsMessage.CancelMailboxChangeError)
                }
                .onSuccess {
                    eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnMailboxCancelEmailChangeSuccess }
                    snackbarDispatcher(SimpleLoginSyncMailboxOptionsMessage.CancelMailboxChangeSuccess)
                }

            actionFlow.update { SimpleLoginSyncMailboxOptionsAction.None }
        }
    }

    internal fun onDeleteMailbox() {
        if (stateFlow.value.canTransferAliases) {
            eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnDeleteMailbox(mailboxId) }
            return
        }

        viewModelScope.launch {
            actionFlow.update { SimpleLoginSyncMailboxOptionsAction.Delete }

            runCatching { deleteSimpleLoginAliasMailbox(mailboxId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error deleting mailbox")
                    PassLogger.w(TAG, error)
                    eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnMailboxDeleteSuccess }
                    snackbarDispatcher(SimpleLoginSyncMailboxOptionsMessage.DeleteMailboxError)
                }
                .onSuccess {
                    eventFlow.update { SimpleLoginSyncMailboxOptionsEvent.OnMailboxDeleteError }
                    snackbarDispatcher(SimpleLoginSyncMailboxOptionsMessage.DeleteMailboxSuccess)
                }

            actionFlow.update { SimpleLoginSyncMailboxOptionsAction.None }
        }

    }

    private companion object {

        private const val TAG = "SimpleLoginSyncMailboxOptionsViewModel"

    }

}
