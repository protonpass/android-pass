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

package proton.android.pass.features.item.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    getItemActions: GetItemActions,
    private val observeItemById: ObserveItemById,
    private val itemDetailsHandler: ItemDetailsHandler
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    internal val state: StateFlow<ItemDetailsState> = observeItemById(shareId, itemId)
        .flatMapLatest { item ->
            combine(
                itemDetailsHandler.observeItemDetails(item),
                oneShot { getItemActions(shareId, itemId) }
            ) { itemDetailsState, itemActions ->
                ItemDetailsState.Success(
                    shareId = shareId,
                    itemId = itemId,
                    itemDetailState = itemDetailsState,
                    itemActions = itemActions
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ItemDetailsState.Loading
        )

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

    internal fun onItemHiddenFieldToggled(
        isVisible: Boolean,
        hiddenState: HiddenState,
        hiddenFieldType: ItemDetailsFieldType.Hidden
    ) {
        viewModelScope.launch {
            itemDetailsHandler.onItemDetailsHiddenFieldToggled(
                isVisible = isVisible,
                hiddenState = hiddenState,
                hiddenFieldType = hiddenFieldType,
                itemCategory = observeItemById(shareId, itemId).first().itemType.category
            )
        }
    }

}
