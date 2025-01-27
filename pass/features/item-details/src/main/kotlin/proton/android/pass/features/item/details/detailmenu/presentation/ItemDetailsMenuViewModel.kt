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

package proton.android.pass.features.item.details.detailmenu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.data.api.usecases.items.UpdateItemFlag
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ItemDetailsMenuViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    getItemById: GetItemById,
    getItemActions: GetItemActions,
    observeShare: ObserveShare,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    private val pinItem: PinItem,
    private val unpinItem: UnpinItem,
    private val updateItemFlag: UpdateItemFlag,
    private val trashItem: TrashItems,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val actionFlow = MutableStateFlow(BottomSheetItemAction.None)

    private val eventFlow = MutableStateFlow<ItemDetailsMenuEvent>(ItemDetailsMenuEvent.Idle)

    private val itemFlow = oneShot { getItemById(shareId, itemId) }

    private val shareFlow = oneShot { observeShare(shareId).first() }

    internal val state: StateFlow<ItemDetailsMenuState> = combine(
        actionFlow,
        eventFlow,
        itemFlow,
        shareFlow
    ) { action, event, item, share ->
        ItemDetailsMenuState(
            action = action,
            event = event,
            itemOption = item.some(),
            itemActionsOption = getItemActions(shareId, itemId).some(),
            shareOption = share.some()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItemDetailsMenuState.Initial
    )

    internal fun onConsumeEvent(event: ItemDetailsMenuEvent) {
        eventFlow.compareAndSet(event, ItemDetailsMenuEvent.Idle)
    }

    internal fun onCopyItemNote() {
        state.value.itemEncryptedNote.let { encryptedNote ->
            if (encryptedNote.isNotEmpty()) {
                encryptionContextProvider.withEncryptionContext {
                    decrypt(encryptedNote)
                }.let { itemNote ->
                    clipboardManager.copyToClipboard(itemNote)
                }.also {
                    viewModelScope.launch {
                        snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemNoteCopied)
                    }

                    eventFlow.update { ItemDetailsMenuEvent.OnItemNoteCopied }
                }
            }
        }
    }

    internal fun onMigrateItem() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.Migrate }

            runCatching { bulkMoveToVaultRepository.save(mapOf(shareId to listOf(itemId))) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error migrating item")
                    PassLogger.w(TAG, error)
                    eventFlow.update { ItemDetailsMenuEvent.OnItemMigrationError }
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemMigrationError)
                }
                .onSuccess {
                    if (state.value.isItemShared) {
                        ItemDetailsMenuEvent.OnItemSharedMigrated
                    } else {
                        ItemDetailsMenuEvent.OnItemMigrated
                    }.also { event ->
                        eventFlow.update { event }
                    }
                }

            actionFlow.update { BottomSheetItemAction.None }
        }
    }

    internal fun onPinItem() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.Pin }

            runCatching { pinItem(shareId, itemId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error pinning item")
                    PassLogger.w(TAG, error)
                    eventFlow.update { ItemDetailsMenuEvent.OnItemPinningError }
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemPinnedError)
                }
                .onSuccess {
                    eventFlow.update { ItemDetailsMenuEvent.OnItemPinned }
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemPinnedSuccess)
                }

            actionFlow.update { BottomSheetItemAction.None }
        }
    }

    internal fun onUnpinItem() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.Unpin }

            runCatching { unpinItem(shareId, itemId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error unpinning item")
                    PassLogger.w(TAG, error)
                    eventFlow.update { ItemDetailsMenuEvent.OnItemUnpinningError }
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemUnpinnedError)
                }
                .onSuccess {
                    eventFlow.update { ItemDetailsMenuEvent.OnItemUnpinned }
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemUnpinnedSuccess)
                }

            actionFlow.update { BottomSheetItemAction.None }
        }
    }

    internal fun onExcludeItemFromMonitoring() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.MonitorExclude }

            runCatching {
                updateItemFlag(
                    shareId = shareId,
                    itemId = itemId,
                    flag = ItemFlag.SkipHealthCheck,
                    isFlagEnabled = true
                )
            }.onFailure { error ->
                PassLogger.w(TAG, "Error excluding item from monitoring")
                PassLogger.w(TAG, error)
                eventFlow.update { ItemDetailsMenuEvent.OnItemMonitorExcluded }
                snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemMonitorExcludedError)
            }.onSuccess {
                eventFlow.update { ItemDetailsMenuEvent.OnItemMonitorExcludedError }
                snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemMonitorExcludedSuccess)
            }

            actionFlow.update { BottomSheetItemAction.None }
        }
    }

    internal fun onIncludeItemInMonitoring() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.MonitorInclude }

            runCatching {
                updateItemFlag(
                    shareId = shareId,
                    itemId = itemId,
                    flag = ItemFlag.SkipHealthCheck,
                    isFlagEnabled = false
                )
            }.onFailure { error ->
                PassLogger.w(TAG, "Error including item in monitoring")
                PassLogger.w(TAG, error)
                eventFlow.update { ItemDetailsMenuEvent.OnItemMonitorIncluded }
                snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemMonitorIncludedError)
            }.onSuccess {
                eventFlow.update { ItemDetailsMenuEvent.OnItemMonitorIncludedError }
                snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemMonitorIncludedSuccess)
            }

            actionFlow.update { BottomSheetItemAction.None }
        }
    }

    internal fun onTrashItem() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.Trash }

            runCatching { trashItem(items = mapOf(shareId to listOf(itemId))) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error trashing item")
                    PassLogger.w(TAG, error)
                    eventFlow.update { ItemDetailsMenuEvent.OnItemTrashingError }
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemTrashedError)
                }
                .onSuccess {
                    eventFlow.update { ItemDetailsMenuEvent.OnItemTrashed }
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemTrashedSuccess)
                }

            actionFlow.update { BottomSheetItemAction.None }
        }
    }

    internal fun onLeaveItem() {
        eventFlow.update { ItemDetailsMenuEvent.OnItemLeaved(shareId) }
    }

    private companion object {

        private const val TAG = "ItemDetailsMenuViewModel"

    }

}
