package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last30Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last60Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last90Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.LastTwoWeeks
import proton.android.pass.commonui.api.DateFormatUtils.Format.LastYear
import proton.android.pass.commonui.api.DateFormatUtils.Format.MoreThan1Year
import proton.android.pass.commonui.api.DateFormatUtils.Format.ThisWeek
import proton.android.pass.commonui.api.DateFormatUtils.Format.Today
import proton.android.pass.commonui.api.DateFormatUtils.Format.Yesterday
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.composecomponents.impl.loading.PassSwipeRefresh
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.pass.domain.ShareId

private const val PLACEHOLDER_ELEMENTS = 40

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    items: ImmutableList<GroupedItemList>,
    shares: ImmutableMap<ShareId, ShareUiModel>,
    isShareSelected: Boolean = false,
    scrollableState: LazyListState = rememberLazyListState(),
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
    LaunchedEffect(shouldScrollToTop) {
        if (shouldScrollToTop && !scrollableState.isScrollInProgress && scrollableState.firstVisibleItemIndex > 0) {
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
        } else if (isLoading == IsLoadingState.Loading) {
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(PLACEHOLDER_ELEMENTS) {
                    PlaceholderItemRow()
                }
            }
        } else if (items.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollableState) {
                header()
                items.forEach { (key, value) ->
                    stickyItemListHeader(key)
                    items(items = value, key = { it.id.id }) { item ->
                        ActionableItemRow(
                            item = item,
                            vaultIcon = shares[item.shareId]
                                ?.takeIf { !isShareSelected }
                                ?.icon
                                ?.toSmallResource(),
                            highlight = highlight,
                            showMenuIcon = showMenuIcon,
                            onItemClick = onItemClick,
                            onItemMenuClick = onItemMenuClick
                        )
                    }
                }
                footer()
            }
        } else {
            emptyContent()
        }
    }
}

@Suppress("ComplexMethod")
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.stickyItemListHeader(key: GroupingKeys) {
    when (key) {
        is GroupingKeys.AlphabeticalKey ->
            stickyHeader { ListHeader(title = key.character.toString()) }

        is GroupingKeys.MonthlyKey ->
            stickyHeader { ListHeader(title = key.monthKey) }

        is GroupingKeys.MostRecentKey -> stickyHeader {
            when (key.formatResultKey) {
                Today -> stringResource(R.string.most_recent_today)
                Yesterday -> stringResource(R.string.most_recent_yesterday)
                ThisWeek -> stringResource(R.string.most_recent_this_week)
                LastTwoWeeks -> stringResource(R.string.most_recent_last_two_weeks)
                Last30Days -> stringResource(R.string.most_recent_last_30_days)
                Last60Days -> stringResource(R.string.most_recent_last_60_days)
                Last90Days -> stringResource(R.string.most_recent_last_90_days)
                LastYear -> stringResource(R.string.most_recent_within_the_last_year)
                MoreThan1Year -> stringResource(R.string.most_recent_more_than_1_year)
                else -> throw IllegalStateException("Unhandled date")
            }.apply { ListHeader(title = this@apply) }
        }

        GroupingKeys.NoGrouping -> {}
    }
}

@Composable
fun ListHeader(modifier: Modifier = Modifier, title: String) {
    Text(
        modifier = modifier
            .padding(16.dp, 0.dp)
            .fillMaxWidth()
            .background(PassTheme.colors.backgroundNorm),
        text = title,
        style = ProtonTheme.typography.captionWeak,
        color = PassTheme.colors.textWeak
    )
}
