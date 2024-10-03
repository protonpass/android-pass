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

package proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.flatMap
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.canUpdate
import proton.android.pass.domain.toPermissions
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ItemOptionsViewModel @Inject constructor(
    private val trashItem: TrashItems,
    private val savedStateHandle: SavedStateHandle,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val getItemById: GetItemById,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    getVaultByShareId: GetVaultByShareId
) : ViewModel() {

    private val userId = UserId(getNavArg(CommonNavArgId.UserId.key))
    private val shareId = ShareId(getNavArg(CommonNavArgId.ShareId.key))
    private val itemId = ItemId(getNavArg(CommonNavArgId.ItemId.key))

    private val eventFlow: MutableStateFlow<ItemOptionsEvent> =
        MutableStateFlow(ItemOptionsEvent.Unknown)
    private val loadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val canModifyFlow: Flow<Boolean> = getVaultByShareId(userId = userId, shareId = shareId)
        .map { vault -> vault.role.toPermissions().canUpdate() }
        .catch {
            PassLogger.w(TAG, "Error getting vault by id")
            PassLogger.w(TAG, it)
            eventFlow.update { ItemOptionsEvent.Close }
            emit(false)
        }
        .distinctUntilChanged()

    val state: StateFlow<ItemOptionsUiState> = combine(
        loadingFlow,
        eventFlow,
        canModifyFlow,
        ::ItemOptionsUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ItemOptionsUiState.Initial
    )

    fun onTrash() = viewModelScope.launch {
        loadingFlow.update { IsLoadingState.Loading }
        runCatching { trashItem(items = mapOf(shareId to listOf(itemId))) }
            .onSuccess {
                eventFlow.update { ItemOptionsEvent.Close }
                snackbarDispatcher(ItemOptionsSnackbarMessage.SentToTrashSuccess)
            }
            .onFailure {
                PassLogger.w(TAG, "Error sending item to trash")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ItemOptionsSnackbarMessage.SentToTrashError)
            }
        loadingFlow.update { IsLoadingState.NotLoading }
    }

    fun onCopyUsername() = viewModelScope.launch {
        getLoginItem().onSuccess {
            clipboardManager.copyToClipboard(it.itemEmail)
            eventFlow.update { ItemOptionsEvent.Close }
            snackbarDispatcher(ItemOptionsSnackbarMessage.UsernameCopiedToClipboard)
        }.onFailure {
            snackbarDispatcher(ItemOptionsSnackbarMessage.CopyToClipboardError)
        }
    }

    fun onCopyPassword() = viewModelScope.launch {
        getLoginItem().onSuccess {
            val password = encryptionContextProvider.withEncryptionContext {
                decrypt(it.password)
            }
            clipboardManager.copyToClipboard(password, isSecure = true)
            eventFlow.update { ItemOptionsEvent.Close }
            snackbarDispatcher(ItemOptionsSnackbarMessage.PasswordCopiedToClipboard)
        }.onFailure {
            snackbarDispatcher(ItemOptionsSnackbarMessage.CopyToClipboardError)
        }
    }

    private suspend fun getLoginItem(): Result<ItemType.Login> = runCatching {
        getItemById(shareId = shareId, itemId = itemId)
    }.flatMap { item ->
        val itemType = item.itemType
        return@flatMap if (itemType !is ItemType.Login) {
            Result.failure(IllegalStateException("Item is not a login"))
        } else {
            Result.success(itemType)
        }
    }

    private fun getNavArg(name: String): String = savedStateHandle.get<String>(name)
        ?: throw IllegalStateException("Missing $name nav argument")

    companion object {
        private const val TAG = "AutofillItemOptionsViewModel"
    }

}


