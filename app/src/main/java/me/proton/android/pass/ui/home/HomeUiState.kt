package me.proton.android.pass.ui.home

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState

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
    val items: List<ItemUiModel>,
    val selectedShare: Option<ShareId> = None
) {
    companion object {
        val Loading = HomeListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            items = emptyList(),
            selectedShare = None
        )
    }
}

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val inSearchMode: Boolean
) {
    companion object {
        val Initial = SearchUiState(
            searchQuery = "",
            inSearchMode = false
        )
    }
}
