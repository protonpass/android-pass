package me.proton.pass.presentation.components.common.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.swiperefresh.SwipeRefreshState
import me.proton.pass.presentation.components.common.PassSwipeRefresh
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState

private const val PLACEHOLDER_ELEMENTS = 40

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    items: List<ItemUiModel>,
    shouldScrollToTop: Boolean,
    highlight: String? = null,
    @StringRes emptyListMessage: Int,
    isRefreshing: IsRefreshingState,
    isLoading: IsLoadingState,
    itemActions: List<ItemAction> = emptyList(),
    onRefresh: () -> Unit,
    onItemClick: (ItemUiModel) -> Unit = {},
    onScrollToTop: () -> Unit
) {
    val scrollableState = rememberLazyListState()
    LaunchedEffect(shouldScrollToTop) {
        if (shouldScrollToTop) {
            scrollableState.scrollToItem(0)
            onScrollToTop()
        }
    }
    PassSwipeRefresh(
        modifier = modifier.fillMaxSize(),
        state = SwipeRefreshState(isRefreshing is IsRefreshingState.Refreshing),
        onRefresh = onRefresh
    ) {
        if (items.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollableState) {
                items(items = items, key = { it.id.id }) { item ->
                    ActionableItemRow(
                        item = item,
                        highlight = highlight,
                        onItemClicked = onItemClick,
                        itemActions = itemActions
                    )
                }
            }
        } else if (isLoading == IsLoadingState.Loading) {
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(PLACEHOLDER_ELEMENTS) {
                    PlaceholderItemRow()
                }
            }
        } else {
            EmptyList(emptyListMessage = stringResource(id = emptyListMessage))
        }
    }
}
