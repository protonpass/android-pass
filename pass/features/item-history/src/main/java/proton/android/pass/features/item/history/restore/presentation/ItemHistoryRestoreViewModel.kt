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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.usecases.GetItemById
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
    private val itemDetailsHandler: ItemDetailsHandler,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getItemById: GetItemById
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

    private val revisionItemContentsUpdateOptionFlow = MutableStateFlow<Option<ItemContents>>(None)

    private val revisionItemDetailsStateFlow: Flow<ItemDetailState> = oneShot {
        encryptionContextProvider.withEncryptionContextSuspendable {
            openItemRevision(shareId, itemRevision, this@withEncryptionContextSuspendable)
        }
    }.flatMapLatest { item ->
        combine(
            revisionItemContentsUpdateOptionFlow,
            itemDetailsHandler.observeItemDetails(item)
        ) { revisionItemContentsUpdateOption, itemDetailState ->
            when (revisionItemContentsUpdateOption) {
                None -> itemDetailState
                is Some -> itemDetailState.update(itemContents = revisionItemContentsUpdateOption.value)
            }
        }
    }

    private val currentItemContentsUpdateOptionFlow = MutableStateFlow<Option<ItemContents>>(None)

    private val currentItemDetailsStateFlow: Flow<ItemDetailState> = oneShot {
        getItemById(shareId, itemId)
    }.flatMapLatest { item ->
        combine(
            currentItemContentsUpdateOptionFlow,
            itemDetailsHandler.observeItemDetails(item)
        ) { currentItemContentsUpdateOption, itemDetailState ->
            when (currentItemContentsUpdateOption) {
                None -> itemDetailState
                is Some -> itemDetailState.update(itemContents = currentItemContentsUpdateOption.value)
            }
        }
    }

    private val eventFlow = MutableStateFlow<ItemHistoryRestoreEvent>(ItemHistoryRestoreEvent.Idle)

    internal val state = combine(
        currentItemDetailsStateFlow,
        revisionItemDetailsStateFlow,
        eventFlow
    ) { currentItemDetailState, revisionItemDetailState, event ->
        ItemHistoryRestoreState.ItemDetails(
            itemRevision = itemRevision,
            currentItemDetailState = currentItemDetailState,
            revisionItemDetailState = revisionItemDetailState,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ItemHistoryRestoreState.Initial
    )

    internal fun onEventConsumed(event: ItemHistoryRestoreEvent) {
        eventFlow.compareAndSet(event, ItemHistoryRestoreEvent.Idle)
    }

    internal fun onItemFieldClicked(text: String, plainFieldType: ItemDetailsFieldType.Plain) {
        viewModelScope.launch {
            itemDetailsHandler.onItemDetailsFieldClicked(text, plainFieldType)
        }
    }

    internal fun onItemHiddenFieldClicked(hiddenState: HiddenState, hiddenFieldType: ItemDetailsFieldType.Hidden) {
        viewModelScope.launch {
            itemDetailsHandler.onItemDetailsHiddenFieldClicked(hiddenState, hiddenFieldType)
        }
    }

    internal fun onToggleItemHiddenField(
        selection: ItemHistoryRestoreSelection,
        isVisible: Boolean,
        hiddenState: HiddenState,
        hiddenFieldType: ItemDetailsFieldType.Hidden
    ) {
        when (val stateValue = state.value) {
            ItemHistoryRestoreState.Initial -> return
            is ItemHistoryRestoreState.ItemDetails -> {
                when (selection) {
                    ItemHistoryRestoreSelection.Revision -> {
                        itemDetailsHandler.updateItemDetailsContent(
                            isVisible = isVisible,
                            hiddenState = hiddenState,
                            hiddenFieldType = hiddenFieldType,
                            itemCategory = stateValue.revisionItemDetailState.itemCategory,
                            itemContents = stateValue.revisionItemDetailState.itemContents
                        ).also { updatedItemContents ->
                            revisionItemContentsUpdateOptionFlow.update { updatedItemContents.some() }
                        }
                    }

                    ItemHistoryRestoreSelection.Current -> {
                        itemDetailsHandler.updateItemDetailsContent(
                            isVisible = isVisible,
                            hiddenState = hiddenState,
                            hiddenFieldType = hiddenFieldType,
                            itemCategory = stateValue.currentItemDetailState.itemCategory,
                            itemContents = stateValue.currentItemDetailState.itemContents
                        ).also { updatedItemContents ->
                            currentItemContentsUpdateOptionFlow.update { updatedItemContents.some() }
                        }
                    }
                }
            }
        }
    }

    internal fun onRestoreItem() {
        eventFlow.update { ItemHistoryRestoreEvent.OnRestoreItem }
    }

    internal fun onRestoreItemCanceled() {
        eventFlow.update { ItemHistoryRestoreEvent.OnRestoreItemCanceled }
    }

    internal fun onRestoreItemConfirmed(itemContents: ItemContents) {
        viewModelScope.launch {
            eventFlow.update { ItemHistoryRestoreEvent.OnRestoreItemConfirmed }

            runCatching { restoreItemRevision(shareId, itemId, itemContents) }
                .onSuccess {
                    eventFlow.update { ItemHistoryRestoreEvent.OnItemRestored }
                    snackbarDispatcher(ItemHistoryRestoreSnackbarMessage.RestoreItemRevisionSuccess)
                }
                .onFailure { error ->
                    PassLogger.w(TAG, "Error restoring item revision: $error")
                    eventFlow.update { ItemHistoryRestoreEvent.OnRestoreItemCanceled }
                    snackbarDispatcher(ItemHistoryRestoreSnackbarMessage.RestoreItemRevisionError)
                }
        }
    }

}
