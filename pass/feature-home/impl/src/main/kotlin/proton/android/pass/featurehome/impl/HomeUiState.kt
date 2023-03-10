package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState

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
    val items: ImmutableMap<GroupingKeys, ImmutableList<ItemUiModel>>,
    val selectedShare: Option<ShareUiModel> = None,
    val sortingType: SortingType = SortingType.MostRecent
) {
    companion object {
        val Loading = HomeListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            items = persistentMapOf(),
            selectedShare = None
        )
    }
}

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val isProcessingSearch: IsProcessingSearchState,
    val inSearchMode: Boolean
) {
    companion object {
        val Initial = SearchUiState(
            searchQuery = "",
            isProcessingSearch = IsProcessingSearchState.NotLoading,
            inSearchMode = false
        )
    }
}
