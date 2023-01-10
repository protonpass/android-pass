package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.loading.PassSwipeRefresh
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState

private const val PLACEHOLDER_ELEMENTS = 40

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    items: ImmutableList<ItemUiModel>,
    shouldScrollToTop: Boolean,
    highlight: String = "",
    isRefreshing: IsRefreshingState,
    isLoading: IsLoadingState,
    isProcessingSearch: IsProcessingSearchState = IsProcessingSearchState.NotLoading,
    showMenuIcon: Boolean = true,
    enableSwipeRefresh: Boolean = true,
    header: LazyListScope.() -> Unit = {},
    footer: LazyListScope.() -> Unit = {},
    onRefresh: () -> Unit,
    onItemClick: (ItemUiModel) -> Unit,
    onItemMenuClick: (ItemUiModel) -> Unit,
    onScrollToTop: () -> Unit,
    emptyContent: @Composable () -> Unit
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
        swipeEnabled = enableSwipeRefresh,
        onRefresh = onRefresh
    ) {
        if (isProcessingSearch == IsProcessingSearchState.Loading) {
            Loading(Modifier.fillMaxSize())
        } else if (items.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollableState) {
                header()
                items(items = items, key = { it.id.id }) { item ->
                    ActionableItemRow(
                        item = item,
                        highlight = highlight,
                        showMenuIcon = showMenuIcon,
                        onItemClick = onItemClick,
                        onItemMenuClick = onItemMenuClick
                    )
                }
                footer()
            }
        } else if (isLoading == IsLoadingState.Loading) {
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(PLACEHOLDER_ELEMENTS) {
                    PlaceholderItemRow()
                }
            }
        } else {
            emptyContent()
        }
    }
}
