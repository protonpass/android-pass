package me.proton.pass.presentation.trash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.item.EmptyList
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
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        onItemMenuClick = onItemMenuClick,
        onRefresh = onRefresh,
        onItemClick = {},
        onScrollToTop = {},
        emptyContent = {
            EmptyList(
                emptyListTitle = stringResource(R.string.trash_empty_list_title),
                emptyListMessage = stringResource(R.string.trash_empty_list_message),
                emptyListImage = R.drawable.empty_trash
            )
        }
    )
}
