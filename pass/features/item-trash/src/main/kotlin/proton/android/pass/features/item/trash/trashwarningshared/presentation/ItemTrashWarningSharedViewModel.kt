/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.item.trash.trashwarningshared.presentation

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
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ItemTrashWarningSharedViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val trashItems: TrashItems,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val eventFlow = MutableStateFlow<ItemTrashWarningSharedEvent>(
        value = ItemTrashWarningSharedEvent.Idle
    )

    private val isLoadingStateFlow = MutableStateFlow<IsLoadingState>(
        value = IsLoadingState.NotLoading
    )

    internal val stateFlow: StateFlow<ItemTrashWarningSharedState> = combine(
        eventFlow,
        isLoadingStateFlow,
        ::ItemTrashWarningSharedState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ItemTrashWarningSharedState.Initial
    )

    internal fun onConsumeEvent(event: ItemTrashWarningSharedEvent) {
        eventFlow.compareAndSet(event, ItemTrashWarningSharedEvent.Idle)
    }

    internal fun onMoveItemToTrash() {
        viewModelScope.launch {
            isLoadingStateFlow.update { IsLoadingState.Loading }

            runCatching { trashItems(items = mapOf(shareId to listOf(itemId))) }
                .onFailure { error ->
                    PassLogger.w(TAG, "Error moving shared item to trash")
                    PassLogger.w(TAG, error)

                    eventFlow.update { ItemTrashWarningSharedEvent.OnMoveItemToTrashError }
                    snackbarDispatcher(ItemTrashWarningSharedMessage.ItemTrashError)
                }
                .onSuccess {
                    eventFlow.update { ItemTrashWarningSharedEvent.OnMoveItemToTrashSuccess }
                    snackbarDispatcher(ItemTrashWarningSharedMessage.ItemTrashSuccess)
                }

            isLoadingStateFlow.update { IsLoadingState.NotLoading }
        }
    }

    private companion object {

        private const val TAG = "ItemTrashWarningSharedViewModel"

    }

}
