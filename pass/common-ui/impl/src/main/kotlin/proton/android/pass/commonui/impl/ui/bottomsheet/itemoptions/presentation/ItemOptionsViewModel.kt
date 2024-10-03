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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.canUpdate
import proton.android.pass.domain.toPermissions
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ItemOptionsViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    getVaultByShareId: GetVaultByShareId,
    getItemById: GetItemById,
    private val trashItem: TrashItems,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider
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

    private val canModifyFlow: Flow<Boolean> = getVaultByShareId(userId = userId, shareId = shareId)
        .map { vault -> vault.role.toPermissions().canUpdate() }
        .catch {
            PassLogger.w(TAG, "Error getting vault by id")
            PassLogger.w(TAG, it)
            eventFlow.update { ItemOptionsEvent.Close }
            emit(false)
        }
        .distinctUntilChanged()

    private val loginItemContentsOptionFlow = oneShot {
        getItemById(shareId = shareId, itemId = itemId)
    }.mapLatest { item ->
        encryptionContextProvider.withEncryptionContext {
            item.toItemContents(this@withEncryptionContext)
        }.let { itemContents ->
            (itemContents as? ItemContents.Login).toOption()
        }
    }

    internal val stateFlow: StateFlow<ItemOptionsState> = combine(
        eventFlow,
        canModifyFlow,
        loginItemContentsOptionFlow,
        isLoadingStateFlow,
        ::ItemOptionsState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ItemOptionsState.Initial
    )

    internal fun onCopyEmail() {
        clipboardManager.copyToClipboard(stateFlow.value.email)
        eventFlow.update { ItemOptionsEvent.Close }

        viewModelScope.launch {
            snackbarDispatcher(ItemOptionsSnackbarMessage.EmailCopiedToClipboard)
        }
    }

    internal fun onCopyUsername() {
        clipboardManager.copyToClipboard(stateFlow.value.username)
        eventFlow.update { ItemOptionsEvent.Close }

        viewModelScope.launch {
            snackbarDispatcher(ItemOptionsSnackbarMessage.UsernameCopiedToClipboard)
        }
    }

    internal fun onCopyPassword() {
        encryptionContextProvider.withEncryptionContext {
            decrypt(stateFlow.value.hiddenStatePassword.encrypted)
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
