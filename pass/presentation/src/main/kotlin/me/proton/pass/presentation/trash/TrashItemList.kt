package me.proton.pass.presentation.trash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.item.ItemsList
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState

@Composable
internal fun TrashItemList(
    modifier: Modifier = Modifier,
    items: List<ItemUiModel>,
    isRefreshing: IsRefreshingState,
    isLoading: IsLoadingState,
    onItemMenuClick: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit
) {
    ItemsList(
        modifier = modifier,
        items = items,
        shouldScrollToTop = false,
        emptyListMessage = R.string.message_no_trashed_credentials,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        onItemMenuClick = onItemMenuClick,
        onRefresh = onRefresh,
        onItemClick = {},
        onScrollToTop = { }
    )
}
