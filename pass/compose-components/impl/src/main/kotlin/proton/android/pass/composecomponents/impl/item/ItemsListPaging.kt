/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.composecomponents.impl.item

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.delay
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.SpecialCharacters.AT_SIGN
import proton.android.pass.common.api.isInstrumentedTest
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last30Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last60Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last90Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.LastTwoWeeks
import proton.android.pass.commonui.api.DateFormatUtils.Format.LastYear
import proton.android.pass.commonui.api.DateFormatUtils.Format.MoreThan1Year
import proton.android.pass.commonui.api.DateFormatUtils.Format.ThisWeek
import proton.android.pass.commonui.api.DateFormatUtils.Format.Today
import proton.android.pass.commonui.api.DateFormatUtils.Format.Yesterday
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonui.api.HomeListItem
import proton.android.pass.commonui.api.TestTags.HOME_LOADING_TAG
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId

private const val PLACEHOLDER_ELEMENTS = 20

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemsListPaging(
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<HomeListItem>,
    shares: ImmutableMap<ShareId, Share>,
    isShareSelected: Boolean = false,
    scrollableState: LazyListState = rememberLazyListState(),
    shouldScrollToTop: Boolean,
    highlight: String = "",
    isRefreshing: IsRefreshingState,
    showMenuIcon: Boolean = true,
    forceShowHeader: Boolean = false,
    accounts: ImmutableMap<UserId, String>,
    header: LazyListScope.() -> Unit = {},
    footer: LazyListScope.() -> Unit = {},
    onRefresh: () -> Unit,
    onItemClick: (ItemUiModel) -> Unit,
    onItemMenuClick: (ItemUiModel) -> Unit,
    onItemLongClick: (ItemUiModel) -> Unit = {},
    onScrollToTop: () -> Unit,
    canLoadExternalImages: Boolean,
    isInSelectionMode: Boolean = false,
    selectedItemIds: ImmutableSet<Pair<ShareId, ItemId>> = persistentSetOf(),
    emptyContent: @Composable () -> Unit
) {
    // to not display the first initial loading
    var canDisplayPullToRefresh by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = pagingItems.loadState.refresh, key2 = pagingItems.itemCount) {
        if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount > 0) {
            canDisplayPullToRefresh = true
        }
    }

    LaunchedEffect(shouldScrollToTop && !scrollableState.isScrollInProgress) {
        if (shouldScrollToTop && !scrollableState.isScrollInProgress) {
            if (!isInstrumentedTest()) {
                scrollableState.scrollToItem(0)
            }
            onScrollToTop()
        }
    }

    var hasSeenRefreshLoading by rememberSaveable { mutableStateOf(false) }

    // workaround to show at the very first launch the skeleton loading instead of the empty screen
    LaunchedEffect(pagingItems.loadState.refresh) {
        if (pagingItems.loadState.refresh is LoadState.Loading) {
            try {
                delay(timeMillis = 300)
            } finally {
                hasSeenRefreshLoading = true
            }
        }
    }

    val listState = remember(
        pagingItems.itemCount,
        pagingItems.loadState.refresh,
        hasSeenRefreshLoading
    ) {
        when {
            pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.Loading -> {
                ItemListPagingState.SkeletonLoading
            }

            pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.NotLoading -> {
                if (hasSeenRefreshLoading) {
                    ItemListPagingState.Empty
                } else {
                    ItemListPagingState.SkeletonLoading
                }
            }

            // A local read error with no items: fall back to the empty state for parity with
            // the non-paginated list, which also swallows errors and shows the empty content.
            pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.Error -> {
                ItemListPagingState.Empty
            }

            else -> ItemListPagingState.Content
        }
    }

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = canDisplayPullToRefresh && isRefreshing.value(),
        onRefresh = {
            onRefresh()
        }
    ) {
        // no need of searchLoading : with pagination it is instant
        // val searchLoading = isProcessingSearch == IsProcessingSearchState.Loading

        AnimatedContent(
            targetState = listState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ItemsListPagingContent"
        ) { state ->
            when (state) {
                ItemListPagingState.SkeletonLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(HOME_LOADING_TAG)
                    ) {
                        repeat(PLACEHOLDER_ELEMENTS) {
                            PlaceholderItemRowNew()
                        }
                    }
                }

                ItemListPagingState.Content -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollableState) {
                        header()
                        items(
                            count = pagingItems.itemCount,
                            key = pagingItems.itemKey { it.key }
                        ) { index ->
                            when (val listItem = pagingItems[index]) {
                                is HomeListItem.Header -> {
                                    StickyItemListHeader(
                                        modifier = Modifier.animateItem(),
                                        groupingKey = listItem.groupingKey
                                    )
                                }

                                is HomeListItem.Item -> {
                                    val item = listItem.itemUiModel
                                    val share = shares[item.shareId]
                                    val isSelectable = share?.canBeSelected ?: false

                                    val icon =
                                        (share as? Share.Vault)?.takeIf { isShareSelected }?.icon
                                    val selection = remember(isInSelectionMode, selectedItemIds) {
                                        val isSelected = item.shareId to item.id in selectedItemIds
                                        ItemSelectionModeState.fromValues(
                                            inSelectionMode = isInSelectionMode,
                                            isSelected = isSelected,
                                            isSelectable = isSelectable
                                        )
                                    }
                                    val titleSuffix = if (accounts.size > 1) {
                                        accounts[item.userId]
                                            ?.split(AT_SIGN)
                                            ?.firstOrNull()
                                            .toOption()
                                    } else {
                                        None
                                    }
                                    ActionableItemRow(
                                        modifier = Modifier.animateItem(),
                                        item = item,
                                        titleSuffix = titleSuffix,
                                        selectionModeState = selection,
                                        vaultIcon = icon?.toSmallResource(),
                                        highlight = highlight,
                                        showMenuIcon = showMenuIcon,
                                        canLoadExternalImages = canLoadExternalImages,
                                        onItemClick = onItemClick,
                                        onItemLongClick = onItemLongClick,
                                        onItemMenuClick = onItemMenuClick
                                    )
                                }

                                null -> {}
                            }
                        }
                        if (pagingItems.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        footer()
                    }
                }

                ItemListPagingState.Empty -> {
                    // During AnimatedContent exit animation, pagingItems may already
                    // be loading new data. Skip rendering to prevent flash.
                    if (pagingItems.loadState.refresh !is LoadState.Loading) {
                        Column {
                            if (forceShowHeader) {
                                LazyColumn { header() }
                            }
                            emptyContent()
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun StickyItemListHeader(modifier: Modifier = Modifier, groupingKey: GroupingKeys) {
    when (groupingKey) {
        is GroupingKeys.AlphabeticalKey ->
            ListHeader(modifier = modifier, title = groupingKey.character.toString())

        is GroupingKeys.MonthlyKey ->
            ListHeader(modifier = modifier, title = groupingKey.monthKey)

        is GroupingKeys.MostRecentKey -> {
            val title = when (groupingKey.formatResultKey) {
                Today -> stringResource(R.string.most_recent_today)
                Yesterday -> stringResource(R.string.most_recent_yesterday)
                ThisWeek -> stringResource(R.string.most_recent_this_week)
                LastTwoWeeks -> stringResource(R.string.most_recent_last_two_weeks)
                Last30Days -> stringResource(R.string.most_recent_last_30_days)
                Last60Days -> stringResource(R.string.most_recent_last_60_days)
                Last90Days -> stringResource(R.string.most_recent_last_90_days)
                LastYear -> stringResource(R.string.most_recent_within_the_last_year)
                MoreThan1Year -> stringResource(R.string.most_recent_more_than_1_year)
                else -> ""
            }
            if (title.isNotEmpty()) {
                ListHeader(modifier = modifier, title = title)
            }
        }

        GroupingKeys.NoGrouping -> Unit
    }
}

private enum class ItemListPagingState {
    SkeletonLoading,
    Content,
    Empty
}
