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

package proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.presentation

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
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.items.GetItemOptions
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ItemOptionsViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val trashItem: TrashItems,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getItemOptions: GetItemOptions
) : ViewModel() {

    private val userId: UserId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.UserId.key)
        .let(::UserId)

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val eventFlow: MutableStateFlow<ItemOptionsEvent> = MutableStateFlow(
        value = ItemOptionsEvent.Idle
    )

    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(
        value = IsLoadingState.NotLoading
    )

    private val itemOptionsFlow = oneShot { getItemOptions(shareId = shareId, itemId = itemId) }

    internal val stateFlow: StateFlow<ItemOptionsState> = combine(
        itemOptionsFlow,
        eventFlow,
        isLoadingStateFlow,
        ::ItemOptionsState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ItemOptionsState.Initial
    )

    internal fun onCopyEmail(email: String) {
        clipboardManager.copyToClipboard(email)
        eventFlow.update { ItemOptionsEvent.Close }

        viewModelScope.launch {
            snackbarDispatcher(ItemOptionsSnackbarMessage.EmailCopiedToClipboard)
        }
    }

    internal fun onCopyUsername(username: String) {
        clipboardManager.copyToClipboard(username)
        eventFlow.update { ItemOptionsEvent.Close }

        viewModelScope.launch {
            snackbarDispatcher(ItemOptionsSnackbarMessage.UsernameCopiedToClipboard)
        }
    }

    internal fun onCopyPassword(encryptedPassword: EncryptedString) {
        encryptionContextProvider.withEncryptionContext {
            decrypt(encryptedPassword)
        }.also { decryptedPassword ->
            clipboardManager.copyToClipboard(decryptedPassword, isSecure = true)
            eventFlow.update { ItemOptionsEvent.Close }
        }

        viewModelScope.launch {
            snackbarDispatcher(ItemOptionsSnackbarMessage.PasswordCopiedToClipboard)
        }
    }

    internal fun onMoveToTrash() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching { trashItem(userId = userId, items = mapOf(shareId to listOf(itemId))) }
                .onSuccess {
                    eventFlow.update { ItemOptionsEvent.Close }
                    snackbarDispatcher(ItemOptionsSnackbarMessage.SentToTrashSuccess)
                }
                .onFailure {
                    PassLogger.w(TAG, "Error sending item to trash")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemOptionsSnackbarMessage.SentToTrashError)
                }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "AutofillItemOptionsViewModel"

    }

}
