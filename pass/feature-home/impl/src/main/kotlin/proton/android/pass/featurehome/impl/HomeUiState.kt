package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState

sealed interface ActionState {
    object Unknown : ActionState
    object Loading : ActionState
    object Done : ActionState
}

@Immutable
data class HomeUiState(
    val homeListUiState: HomeListUiState,
    val searchUiState: SearchUiState
) {
    companion object {
        val Loading = HomeUiState(
            homeListUiState = HomeListUiState.Loading,
            searchUiState = SearchUiState.Initial
        )
    }
}

@Immutable
data class HomeListUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val actionState: ActionState = ActionState.Unknown,
    val items: ImmutableList<GroupedItemList>,
    val selectedShare: Option<ShareUiModel> = None,
    val homeVaultSelection: HomeVaultSelection = HomeVaultSelection.AllVaults,
    val homeItemTypeSelection: HomeItemTypeSelection = HomeItemTypeSelection.AllItems,
    val sortingType: SortingType = SortingType.MostRecent,
) {
    companion object {
        val Loading = HomeListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            items = persistentListOf(),
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
            itemTypeCount = ItemTypeCount(0, 0, 0)
        )
    }
}
