package proton.android.pass.featuretrash.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState

@Composable
internal fun TrashItemList(
    modifier: Modifier = Modifier,
    items: ImmutableMap<GroupingKeys, ImmutableList<ItemUiModel>>,
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
        emptyContent = { EmptyTrashContent() }
    )
}
