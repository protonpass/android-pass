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

package proton.android.pass.features.item.history.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.items.GetItemCategory
import proton.android.pass.data.api.usecases.items.ObserveItemRevisions
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class ItemHistoryTimelineViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    observeItemRevisions: ObserveItemRevisions,
    getItemCategory: GetItemCategory,
    snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    internal val state: StateFlow<ItemHistoryTimelineState> = combine(
        observeItemRevisions(shareId, itemId),
        oneShot { getItemCategory(shareId, itemId) }
    ) { itemRevisions, itemCategory ->
        ItemHistoryTimelineState.Success(
            shareId = shareId,
            itemId = itemId,
            itemRevisions = itemRevisions.toPersistentList(),
            itemRevisionCategory = itemCategory
        )
    }
        .catch<ItemHistoryTimelineState> {
            emit(ItemHistoryTimelineState.Error)
            snackbarDispatcher(ItemHistoryTimelineSnackbarMessage.FetchItemRevisionsError)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ItemHistoryTimelineState.Loading
        )

}
