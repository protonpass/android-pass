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

package proton.android.pass.featureselectitem.ui

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.domain.ShareId
import proton.android.pass.featuresearchoptions.api.SearchSortingType

sealed interface SearchInMode {
    data object OldestVaults : SearchInMode
    data object AllVaults : SearchInMode
    data object Uninitialized : SearchInMode
}

@Immutable
data class SelectItemUiState(
    val listUiState: SelectItemListUiState,
    val searchUiState: SearchUiState,
    val pinningUiState: PinningUiState
) {

    fun shouldShowItemListHeader() = listUiState.items.items.isNotEmpty() &&
        listUiState.isLoading == IsLoadingState.NotLoading &&
        !searchUiState.isProcessingSearch.value()

    companion object {
        val Loading = SelectItemUiState(
            listUiState = SelectItemListUiState.Loading,
            searchUiState = SearchUiState.Initial,
            pinningUiState = PinningUiState.Initial
        )
    }
}

data class SelectItemListUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val itemClickedEvent: AutofillItemClickedEvent,
    val items: SelectItemListItems,
    val shares: PersistentMap<ShareId, ShareUiModel>,
    val sortingType: SearchSortingType,
    val shouldScrollToTop: Boolean,
    val canLoadExternalImages: Boolean,
    val displayOnlyPrimaryVaultMessage: Boolean,
    val canUpgrade: Boolean,
    val displayCreateButton: Boolean
) {

    val itemCount: Int =
        items.items.map { it.items }.flatten().count() + items.suggestions.count()

    companion object {
        val Loading = SelectItemListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            itemClickedEvent = AutofillItemClickedEvent.None,
            items = SelectItemListItems.Initial,
            shares = persistentMapOf(),
            sortingType = SearchSortingType.MostRecent,
            shouldScrollToTop = false,
            canLoadExternalImages = false,
            displayOnlyPrimaryVaultMessage = false,
            canUpgrade = true,
            displayCreateButton = false
        )
    }
}

data class SelectItemListItems(
    val suggestions: ImmutableList<ItemUiModel>,
    val items: ImmutableList<GroupedItemList>,
    val suggestionsForTitle: String
) {
    companion object {
        val Initial = SelectItemListItems(
            suggestions = persistentListOf(),
            items = persistentListOf(),
            suggestionsForTitle = ""
        )
    }
}

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val inSearchMode: Boolean,
    val isProcessingSearch: IsProcessingSearchState,
    val searchInMode: SearchInMode

) {
    companion object {
        val Initial = SearchUiState(
            searchQuery = "",
            inSearchMode = false,
            isProcessingSearch = IsProcessingSearchState.NotLoading,
            searchInMode = SearchInMode.Uninitialized
        )
    }
}

@Immutable
data class PinningUiState(
    val inPinningMode: Boolean,
    val filteredItems: ImmutableList<GroupedItemList>,
    val unFilteredItems: PersistentList<ItemUiModel>
) {

    val itemCount = filteredItems.map { it.items }.flatten().count()

    companion object {
        val Initial = PinningUiState(
            inPinningMode = false,
            filteredItems = persistentListOf(),
            unFilteredItems = persistentListOf()
        )
    }
}
