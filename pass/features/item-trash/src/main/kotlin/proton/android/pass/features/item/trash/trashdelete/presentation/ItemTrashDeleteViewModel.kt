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

package proton.android.pass.features.item.trash.trashdelete.presentation

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
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.DeleteItems
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class ItemTrashDeleteViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val deleteItem: DeleteItems,
    private val getItemById: GetItemById,
    private val telemetryManager: TelemetryManager,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val eventFlow = MutableStateFlow<ItemTrashDeleteEvent>(ItemTrashDeleteEvent.Idle)

    private val isLoadingStateFlow = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)

    internal val state: StateFlow<ItemTrashDeleteState> = combine(
        eventFlow,
        isLoadingStateFlow,
        ::ItemTrashDeleteState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ItemTrashDeleteState.Initial
    )

    internal fun onConsumeEvent(event: ItemTrashDeleteEvent) {
        eventFlow.compareAndSet(event, ItemTrashDeleteEvent.Idle)
    }

    internal fun onDeleteItem() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }
            val itemType = runCatching {
                getItemById(shareId = shareId, itemId = itemId).itemType
            }.onFailure {
                PassLogger.w(TAG, "Failed to load item before delete for telemetry")
            }.getOrNull()
            runCatching { deleteItem(items = mapOf(shareId to listOf(itemId))) }
                .onFailure { error ->
                    PassLogger.w(TAG, "There was an error deleting trashed item")
                    PassLogger.w(TAG, error)
                    eventFlow.update { ItemTrashDeleteEvent.OnItemDeleteError }
                    snackbarDispatcher(ItemTrashDeleteSnackBarMessage.ItemDeleteError)
                }
                .onSuccess {
                    itemType?.let {
                        telemetryManager.sendEvent(ItemDelete(EventItemType.from(it)))
                    }
                    eventFlow.update { ItemTrashDeleteEvent.OnItemDeleted }
                    snackbarDispatcher(ItemTrashDeleteSnackBarMessage.ItemDeleted)
                }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "ItemTrashDeleteViewModel"

    }

}
