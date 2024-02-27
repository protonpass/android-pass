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

package proton.android.pass.features.item.history.restore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.usecases.items.OpenItemRevision
import proton.android.pass.data.api.usecases.items.RestoreItemRevision
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.item.history.navigation.ItemHistoryRevisionNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

private const val TAG = "ItemHistoryRestoreViewModel"

@HiltViewModel
class ItemHistoryRestoreViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    openItemRevision: OpenItemRevision,
    private val restoreItemRevision: RestoreItemRevision,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val itemDetailsHandler: ItemDetailsHandler,
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val itemRevision: ItemRevision = savedStateHandleProvider.get()
        .require<String>(ItemHistoryRevisionNavArgId.key)
        .let { encodedRevision -> Json.decodeFromString(NavParamEncoder.decode(encodedRevision)) }

    private val eventFlow = MutableStateFlow<ItemHistoryRestoreEvent>(ItemHistoryRestoreEvent.Idle)

    private val itemDetailsStateFlow: Flow<ItemDetailState> =
        oneShot { openItemRevision(shareId, itemRevision) }
            .flatMapLatest(itemDetailsHandler::observeItemDetails)

    internal val state = combine(
        itemDetailsStateFlow,
        eventFlow,
    ) { itemDetailState, event ->
        ItemHistoryRestoreState.ItemDetails(
            itemRevision = itemRevision,
            itemDetailState = itemDetailState,
            event = event,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ItemHistoryRestoreState.Initial,
    )

    internal fun onEventConsumed(event: ItemHistoryRestoreEvent) {
        eventFlow.compareAndSet(event, ItemHistoryRestoreEvent.Idle)
    }

    internal fun onItemFieldClicked(text: String) {
        itemDetailsHandler.onItemDetailsFieldClicked(text)
    }

    internal fun onItemHiddenFieldClicked(hiddenState: HiddenState) {
        itemDetailsHandler.onItemDetailsHiddenFieldClicked(hiddenState)
    }

    internal fun onItemHiddenFieldToggled(
        isVisible: Boolean,
        hiddenState: HiddenState,
    ) = viewModelScope.launch {
        itemDetailsHandler.onItemDetailsHiddenFieldToggled(
            isVisible = isVisible,
            hiddenState = hiddenState,
            itemCategory = itemDetailsStateFlow.first().itemCategory,
        )
    }

    internal fun onRestoreItem() {
        eventFlow.update { ItemHistoryRestoreEvent.OnRestoreItem }
    }

    internal fun onRestoreItemCanceled() {
        eventFlow.update { ItemHistoryRestoreEvent.OnRestoreItemCanceled }
    }

    internal fun onRestoreItemConfirmed(itemContents: ItemContents) = viewModelScope.launch {
        eventFlow.update { ItemHistoryRestoreEvent.OnRestoreItemConfirmed }

        runCatching { restoreItemRevision(shareId, itemId, itemContents) }
            .onSuccess {
                eventFlow.update { ItemHistoryRestoreEvent.OnItemRestored }
                snackbarDispatcher.invoke(ItemHistoryRestoreSnackbarMessage.RestoreItemRevisionSuccess)
            }
            .onFailure { error ->
                PassLogger.w(TAG, "Error restoring item revision: $error")
                snackbarDispatcher.invoke(ItemHistoryRestoreSnackbarMessage.RestoreItemRevisionError)
            }
    }

}
