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

package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.featuresearchoptions.api.SearchFilterType
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption

sealed interface ActionState {
    object Unknown : ActionState
    object Loading : ActionState
    object Done : ActionState
}

sealed interface HomeNavEvent {
    object Unknown : HomeNavEvent
    object ShowBulkMoveToVault : HomeNavEvent
}

@Immutable
data class HomeUiState(
    val homeListUiState: HomeListUiState,
    val searchUiState: SearchUiState,
    val accountType: AccountType,
    val navEvent: HomeNavEvent,
) {
    fun shouldShowRecentSearchHeader() =
        homeListUiState.items.isNotEmpty() && searchUiState.inSearchMode && searchUiState.isInSuggestionsMode

    fun shouldShowItemListHeader() =
        homeListUiState.items.isNotEmpty() &&
            homeListUiState.isLoading == IsLoadingState.NotLoading &&
            !searchUiState.isInSuggestionsMode &&
            !searchUiState.isProcessingSearch.value() &&
            searchUiState.inSearchMode

    fun isSelectedVaultReadOnly() =
        when (val selection = homeListUiState.homeVaultSelection) {
            is VaultSelectionOption.AllVaults -> false
            is VaultSelectionOption.Vault ->
                homeListUiState.shares[selection.shareId]?.role == ShareRole.Read

            is VaultSelectionOption.Trash -> false
        }

    companion object {
        val Loading = HomeUiState(
            homeListUiState = HomeListUiState.Loading,
            searchUiState = SearchUiState.Initial,
            accountType = AccountType.Free,
            navEvent = HomeNavEvent.Unknown
        )
    }
}

@Immutable
data class HomeSelectionState(
    val selectedItems: ImmutableSet<Pair<ShareId, ItemId>>,
    val isInSelectMode: Boolean
) {
    companion object {
        val Initial = HomeSelectionState(persistentSetOf(), false)
    }
}

@Immutable
data class HomeListUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val shouldScrollToTop: Boolean,
    val canLoadExternalImages: Boolean,
    val actionState: ActionState = ActionState.Unknown,
    val items: ImmutableList<GroupedItemList>,
    val selectedShare: Option<ShareUiModel> = None,
    val shares: ImmutableMap<ShareId, ShareUiModel>,
    val homeVaultSelection: VaultSelectionOption = VaultSelectionOption.AllVaults,
    val searchFilterType: SearchFilterType = SearchFilterType.All,
    val sortingType: SearchSortingType = SearchSortingType.MostRecent,
    val selectionState: HomeSelectionState
) {
    companion object {
        val Loading = HomeListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            shouldScrollToTop = false,
            canLoadExternalImages = false,
            items = persistentListOf(),
            shares = persistentMapOf(),
            selectionState = HomeSelectionState.Initial
        )
    }
}

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val isProcessingSearch: IsProcessingSearchState,
    val inSearchMode: Boolean,
    val isInSuggestionsMode: Boolean,
    val itemTypeCount: ItemTypeCount
) {
    companion object {
        val Initial = SearchUiState(
            searchQuery = "",
            isProcessingSearch = IsProcessingSearchState.NotLoading,
            inSearchMode = false,
            isInSuggestionsMode = false,
            itemTypeCount = ItemTypeCount(0, 0, 0, 0)
        )
    }
}
