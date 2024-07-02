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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ItemDetailsMenuViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeItemById: ObserveItemById,
    private val pinItem: PinItem,
    private val unpinItem: UnpinItem,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val actionFlow = MutableStateFlow<BottomSheetItemAction>(BottomSheetItemAction.None)

    private val eventFlow = MutableStateFlow<ItemDetailsMenuEvent>(ItemDetailsMenuEvent.Idle)

    internal val state: StateFlow<ItemDetailsMenuState> = combine(
        observeItemById(shareId, itemId),
        actionFlow,
        eventFlow
    ) { item, action, event ->
        ItemDetailsMenuState(
            itemCategory = item.itemType.category,
            isItemPinned = item.isPinned,
            action = action,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItemDetailsMenuState.Initial
    )

    internal fun onConsumeEvent(event: ItemDetailsMenuEvent) {
        eventFlow.compareAndSet(event, ItemDetailsMenuEvent.Idle)
    }

    internal fun onPinItem() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.Pin }

            runCatching { pinItem(shareId, itemId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error pinning item")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemPinnedError)
                }
                .onSuccess {
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemPinnedSuccess)
                }

            actionFlow.update { BottomSheetItemAction.None }
            eventFlow.update { ItemDetailsMenuEvent.OnItemPinned }
        }
    }

    internal fun onUnpinItem() {
        viewModelScope.launch {
            actionFlow.update { BottomSheetItemAction.Unpin }

            runCatching { unpinItem(shareId, itemId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error unpinning item")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemUnpinnedError)
                }
                .onSuccess {
                    snackbarDispatcher(ItemDetailMenuSnackBarMessage.ItemUnpinnedSuccess)
                }

            actionFlow.update { BottomSheetItemAction.None }
            eventFlow.update { ItemDetailsMenuEvent.OnItemUnpinned }
        }
    }

    private companion object {

        private const val TAG = "ItemDetailsMenuViewModel"

    }

}
