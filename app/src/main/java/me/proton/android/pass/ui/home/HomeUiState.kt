package me.proton.android.pass.ui.home

import androidx.compose.runtime.Immutable
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.IsRefreshingState

@Immutable
data class HomeUiState(
    val homeListUiState: HomeListUiState,
    val searchUiState: SearchUiState
)

@Immutable
data class HomeListUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val items: List<ItemUiModel>,
    val selectedShare: Option<ShareId> = None,
    val errorMessage: Option<String> = None
)

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val inSearchMode: Boolean
)
