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

package proton.android.pass.features.sl.sync.mailboxes.delete.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.simplelogin.DeleteSimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasMailboxes
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.features.sl.sync.shared.navigation.mailboxes.SimpleLoginSyncMailboxIdNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncMailboxDeleteViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeSimpleLoginAliasMailbox: ObserveSimpleLoginAliasMailbox,
    observeSimpleLoginAliasMailboxes: ObserveSimpleLoginAliasMailboxes,
    private val deleteSimpleLoginAliasMailbox: DeleteSimpleLoginAliasMailbox,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val mailboxId = savedStateHandleProvider.get()
        .require<Long>(SimpleLoginSyncMailboxIdNavArgId.key)

    private val transferAliasMailboxesFlow = observeSimpleLoginAliasMailboxes()
        .mapLatest { aliasMailboxes ->
            aliasMailboxes.filter { aliasMailbox ->
                aliasMailbox.id != mailboxId && aliasMailbox.isVerified
            }
        }

    private val isTransferAliasesEnabledFlow = MutableStateFlow(true)

    private val aliasMailboxOptionFlow =
        oneShot { observeSimpleLoginAliasMailbox(mailboxId).first() }
            .mapLatest { aliasMailbox ->
                requireNotNull(aliasMailbox).some()
            }
            .catch { error ->
                PassLogger.w(TAG, "There was an error observing alias mailbox")
                PassLogger.w(TAG, error)
                eventFlow.update { SimpleLoginSyncMailboxDeleteEvent.OnDeleteAliasMailboxError }
                snackbarDispatcher(SimpleLoginSyncMailboxDeleteMessage.DeleteAliasMailboxError)
                emit(None)
            }

    private val eventFlow = MutableStateFlow<SimpleLoginSyncMailboxDeleteEvent>(
        value = SimpleLoginSyncMailboxDeleteEvent.Idle
    )

    private val isLoadingStateFlow = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)

    private val selectedAliasMailboxOptionFlow = MutableStateFlow<Option<SimpleLoginAliasMailbox>>(
        value = None
    )

    internal val stateFlow: StateFlow<SimpleLoginSyncMailboxDeleteState> = combineN(
        transferAliasMailboxesFlow,
        isTransferAliasesEnabledFlow,
        eventFlow,
        aliasMailboxOptionFlow,
        selectedAliasMailboxOptionFlow,
        isLoadingStateFlow,
        ::SimpleLoginSyncMailboxDeleteState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SimpleLoginSyncMailboxDeleteState.Initial
    )

    internal fun onToggleTransferAliases(isTransferAliasesEnabled: Boolean) {
        isTransferAliasesEnabledFlow.update { isTransferAliasesEnabled }
    }

    internal fun onSelectTransferAliasMailbox(selectedAliasMailbox: SimpleLoginAliasMailbox) {
        selectedAliasMailboxOptionFlow.update { selectedAliasMailbox.some() }
    }

    internal fun onDeleteMailbox() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching {
                deleteSimpleLoginAliasMailbox(
                    mailboxId = mailboxId,
                    transferMailboxId = stateFlow.value.transferAliasMailboxId.takeIf {
                        stateFlow.value.isTransferAliasesEnabled
                    }
                )
            }.onFailure { error ->
                PassLogger.w(TAG, "There was an error deleting alias mailbox")
                PassLogger.w(TAG, error)
                eventFlow.update { SimpleLoginSyncMailboxDeleteEvent.OnDeleteAliasMailboxError }
                snackbarDispatcher(SimpleLoginSyncMailboxDeleteMessage.DeleteAliasMailboxError)
            }.onSuccess {
                eventFlow.update { SimpleLoginSyncMailboxDeleteEvent.OnDeleteAliasMailboxSuccess }
                snackbarDispatcher(SimpleLoginSyncMailboxDeleteMessage.DeleteAliasMailboxSuccess)
            }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "SimpleLoginSyncMailboxDeleteViewModel"

    }

}
