package me.proton.pass.presentation.trash

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.android.pass.composecomponents.impl.uievents.IsRefreshingState

@Immutable
data class TrashUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val items: ImmutableList<ItemUiModel>
) {
    companion object {
        val Loading = TrashUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            items = persistentListOf()
        )
    }
}
