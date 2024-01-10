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

package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import proton.android.pass.autofill.ui.autofill.common.AutofillConfirmMode
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.domain.ShareId
import proton.android.pass.featuresearchoptions.api.SearchSortingType

sealed interface SearchInMode {
    object OldestVaults : SearchInMode
    object AllVaults : SearchInMode
    object Uninitialized : SearchInMode
}

@Immutable
data class SelectItemUiState(
    val listUiState: SelectItemListUiState,
    val searchUiState: SearchUiState,
    val confirmMode: Option<AutofillConfirmMode>
) {

    fun shouldShowItemListHeader() =
        listUiState.items.items.isNotEmpty() &&
            listUiState.isLoading == IsLoadingState.NotLoading &&
            searchUiState.inSearchMode &&
            !searchUiState.isProcessingSearch.value()

    companion object {
        val Loading = SelectItemUiState(
            listUiState = SelectItemListUiState.Loading,
            searchUiState = SearchUiState.Initial,
            confirmMode = None
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
    val canUpgrade: Boolean
) {
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
            canUpgrade = true
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
