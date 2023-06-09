package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.pass.domain.ShareId

sealed interface ActionState {
    object Unknown : ActionState
    object Loading : ActionState
    object Done : ActionState
}

@Immutable
data class HomeUiState(
    val homeListUiState: HomeListUiState,
    val searchUiState: SearchUiState,
    val accountType: AccountType
) {
    companion object {
        val Loading = HomeUiState(
            homeListUiState = HomeListUiState.Loading,
            searchUiState = SearchUiState.Initial,
            accountType = AccountType.Free
        )
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
    val homeItemTypeSelection: HomeItemTypeSelection = HomeItemTypeSelection.AllItems,
    val sortingType: SearchSortingType = SearchSortingType.MostRecent
) {
    companion object {
        val Loading = HomeListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            shouldScrollToTop = false,
            canLoadExternalImages = false,
            items = persistentListOf(),
            shares = persistentMapOf(),
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
