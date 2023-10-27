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

package proton.android.pass.featureitemdetail.impl.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsPermanentlyDeletedState
import proton.android.pass.composecomponents.impl.uievents.IsRestoredFromTrashState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.RestoreItem
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.data.api.usecases.capabilities.CanMigrateVault
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.InitError
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotRestored
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemPermanentlyDeleted
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemRestored
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.NoteCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.ItemDelete
import proton.android.pass.featureitemdetail.impl.common.ShareClickAction
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.canUpdate
import proton.pass.domain.toPermissions
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val trashItem: TrashItem,
    private val deleteItem: DeleteItem,
    private val restoreItem: RestoreItem,
    private val telemetryManager: TelemetryManager,
    private val clipboardManager: ClipboardManager,
    private val canShareVault: CanShareVault,
    private val canMigrate: CanMigrateVault,
    canPerformPaidAction: CanPerformPaidAction,
    getItemByIdWithVault: GetItemByIdWithVault,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shareId: ShareId = ShareId(savedStateHandle.require(CommonNavArgId.ShareId.key))
    private val itemId: ItemId = ItemId(savedStateHandle.require(CommonNavArgId.ItemId.key))

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val isPermanentlyDeletedState: MutableStateFlow<IsPermanentlyDeletedState> =
        MutableStateFlow(IsPermanentlyDeletedState.NotDeleted)
    private val isRestoredFromTrashState: MutableStateFlow<IsRestoredFromTrashState> =
        MutableStateFlow(IsRestoredFromTrashState.NotRestored)

    private val canPerformPaidActionFlow: Flow<LoadingResult<Boolean>> =
        canPerformPaidAction().asLoadingResult()

    private val shareActionFlow: Flow<ShareClickAction> = canPerformPaidActionFlow
        .map { isPaidResult ->
            val isPaid = isPaidResult.getOrNull() ?: false
            val canShareVault = canShareVault(shareId).value()
            when {
                isPaid && canShareVault -> ShareClickAction.Share
                else -> ShareClickAction.Upgrade
            }
        }
        .distinctUntilChanged()

    val state: StateFlow<NoteDetailUiState> = combineN(
        getItemByIdWithVault(shareId, itemId).asLoadingResult(),
        isLoadingState,
        isItemSentToTrashState,
        isPermanentlyDeletedState,
        isRestoredFromTrashState,
        shareActionFlow
    ) { itemLoadingResult,
        isLoading,
        isItemSentToTrash,
        isPermanentlyDeleted,
        isRestoredFromTrash,
        shareAction ->
        when (itemLoadingResult) {
            is LoadingResult.Error -> {
                snackbarDispatcher(InitError)
                NoteDetailUiState.Error
            }

            LoadingResult.Loading -> NoteDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                val details = itemLoadingResult.data
                val vault = details.vault.takeIf { details.hasMoreThanOneVault }
                val canMigrate = canMigrate(details.vault.shareId)

                val permissions = details.vault.role.toPermissions()
                val canPerformItemActions = permissions.canUpdate()

                NoteDetailUiState.Success(
                    itemUiModel = encryptionContextProvider.withEncryptionContext {
                        details.item.toUiModel(this)
                    },
                    vault = vault,
                    isLoading = isLoading.value(),
                    isItemSentToTrash = isItemSentToTrash.value(),
                    isPermanentlyDeleted = isPermanentlyDeleted.value(),
                    isRestoredFromTrash = isRestoredFromTrash.value(),
                    canMigrate = canMigrate,
                    canPerformActions = canPerformItemActions,
                    shareClickAction = shareAction
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NoteDetailUiState.NotInitialised
        )

    fun onMoveToTrash(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { trashItem(shareId = shareId, itemId = itemId) }
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarDispatcher(ItemMovedToTrash)
            }
            .onFailure {
                snackbarDispatcher(ItemNotMovedToTrash)
                PassLogger.d(TAG, it, "Could not delete item")
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onPermanentlyDelete(itemUiModel: ItemUiModel) =
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            runCatching {
                deleteItem(shareId = itemUiModel.shareId, itemId = itemUiModel.id)
            }.onSuccess {
                telemetryManager.sendEvent(ItemDelete(EventItemType.from(itemUiModel.contents)))
                isPermanentlyDeletedState.update { IsPermanentlyDeletedState.Deleted }
                snackbarDispatcher(ItemPermanentlyDeleted)
                PassLogger.i(TAG, "Item deleted successfully")
            }.onFailure {
                snackbarDispatcher(DetailSnackbarMessages.ItemNotPermanentlyDeleted)
                PassLogger.i(TAG, it, "Could not delete item")
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }

    fun onItemRestore(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            restoreItem(shareId = shareId, itemId = itemId)
        }.onSuccess {
            isRestoredFromTrashState.update { IsRestoredFromTrashState.Restored }
            PassLogger.i(TAG, "Item restored successfully")
            snackbarDispatcher(ItemRestored)
        }.onFailure {
            PassLogger.i(TAG, it, "Error restoring item")
            snackbarDispatcher(ItemNotRestored)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onCopyToClipboard(itemUiModel: ItemUiModel) = viewModelScope.launch {
        val contents = itemUiModel.contents as? ItemContents.Note ?: return@launch
        clipboardManager.copyToClipboard(contents.note, isSecure = false)
        snackbarDispatcher(NoteCopiedToClipboard)
    }

    companion object {
        private const val TAG = "NoteDetailViewModel"
    }
}
