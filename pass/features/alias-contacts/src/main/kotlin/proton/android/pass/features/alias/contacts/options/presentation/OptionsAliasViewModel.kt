/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.alias.contacts.options.presentation

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
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.aliascontact.DeleteAliasContact
import proton.android.pass.data.api.usecases.aliascontact.ObserveAliasContact
import proton.android.pass.data.api.usecases.aliascontact.UpdateBlockedAliasContact
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.aliascontacts.ContactId
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.ContactBlockError
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.ContactBlockSuccess
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.ContactUnblockError
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.ContactUnblockSuccess
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.DeleteContactError
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.DeleteContactSuccess
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.EmailCopiedToClipboard
import proton.android.pass.features.alias.contacts.ContactIdNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class OptionsAliasViewModel @Inject constructor(
    private val deleteAliasContact: DeleteAliasContact,
    private val updateBlockedAliasContact: UpdateBlockedAliasContact,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeAliasContact: ObserveAliasContact
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val contactId: ContactId = savedStateHandleProvider.get()
        .require<Int>(ContactIdNavArgId.key)
        .let(::ContactId)

    private val eventFlow: MutableStateFlow<OptionsAliasEvent> = MutableStateFlow(
        value = OptionsAliasEvent.Idle
    )

    private val isBlockLoadingStateFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(
        value = IsLoadingState.NotLoading
    )
    private val isDeleteLoadingStateFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(
        value = IsLoadingState.NotLoading
    )

    internal val state: StateFlow<OptionsAliasUIState> = combine(
        eventFlow,
        isBlockLoadingStateFlow,
        isDeleteLoadingStateFlow,
        observeAliasContact(shareId, itemId, contactId).asResultWithoutLoading()
    ) { event, isBlockLoadingState, isDeleteLoadingState, aliasContactResult ->
        OptionsAliasUIState(
            event = event,
            isBlockLoading = isBlockLoadingState.value(),
            isDeleteLoading = isDeleteLoadingState.value(),
            contact = aliasContactResult.getOrNull()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = OptionsAliasUIState.Initial
        )

    internal fun onCopyEmail() {
        val email = state.value.contact?.reverseAlias ?: return
        clipboardManager.copyToClipboard(email)
        viewModelScope.launch {
            snackbarDispatcher(EmailCopiedToClipboard)
        }
        eventFlow.update { OptionsAliasEvent.Close }
    }

    fun onDeleteContact() {
        viewModelScope.launch {
            isBlockLoadingStateFlow.update { IsLoadingState.Loading }
            runCatching {
                deleteAliasContact(shareId, itemId, contactId)
            }.onSuccess {
                PassLogger.i(TAG, "Contact deleted")
                snackbarDispatcher(DeleteContactSuccess)
            }.onError {
                PassLogger.w(TAG, "Error deleting contact")
                PassLogger.w(TAG, it)
                snackbarDispatcher(DeleteContactError)
            }
            isBlockLoadingStateFlow.update { IsLoadingState.NotLoading }
            eventFlow.update { OptionsAliasEvent.Close }
        }
    }

    fun onBlockContact() {
        viewModelScope.launch {
            isBlockLoadingStateFlow.update { IsLoadingState.Loading }
            runCatching {
                updateBlockedAliasContact(shareId, itemId, contactId, blocked = true)
            }.onSuccess {
                PassLogger.i(TAG, "Contact blocked")
                snackbarDispatcher(ContactBlockSuccess)
            }.onError {
                PassLogger.w(TAG, "Error blocking contact")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ContactBlockError)
            }
            isBlockLoadingStateFlow.update { IsLoadingState.NotLoading }
            eventFlow.update { OptionsAliasEvent.Close }
        }
    }

    fun onUnblockContact() {
        viewModelScope.launch {
            isBlockLoadingStateFlow.update { IsLoadingState.Loading }
            runCatching {
                updateBlockedAliasContact(shareId, itemId, contactId, blocked = false)
            }.onSuccess {
                PassLogger.i(TAG, "Contact unblocked")
                snackbarDispatcher(ContactUnblockSuccess)
            }.onError {
                PassLogger.w(TAG, "Error unblocking contact")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ContactUnblockError)
            }
            isBlockLoadingStateFlow.update { IsLoadingState.NotLoading }
            eventFlow.update { OptionsAliasEvent.Close }
        }
    }

    fun onSendEmail() {
        val email = state.value.contact?.reverseAlias ?: return
        eventFlow.update { OptionsAliasEvent.SendEmail(email) }
    }

    internal fun onConsumeEvent(event: OptionsAliasEvent) {
        eventFlow.compareAndSet(event, OptionsAliasEvent.Idle)
    }

    private companion object {
        private const val TAG = "OptionsAliasViewModel"
    }
}
