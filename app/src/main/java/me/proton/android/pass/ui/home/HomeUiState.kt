package me.proton.android.pass.ui.home

import androidx.compose.runtime.Immutable
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.model.ItemUiModel

@Immutable
data class HomeUiState(
    val homeListUiState: HomeListUiState,
    val searchUiState: SearchUiState
)

@Immutable
data class HomeListUiState(
    val isLoading: Boolean,
    val isRefreshing: Boolean,

    val items: List<ItemUiModel>,
    val selectedShare: ShareId? = null,

    val errorMessage: String? = null
)

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val inSearchMode: Boolean
)
