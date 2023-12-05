/*
 * Copyright (c) 2023 Proton AG
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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.common.api.isInstrumentedTest
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
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions

private const val PLACEHOLDER_ELEMENTS = 40

@OptIn(ExperimentalMaterialApi::class)
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
    forceShowHeader: Boolean = false,
    header: LazyListScope.() -> Unit = {},
    forceContent: Boolean = false,
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
    LaunchedEffect(shouldScrollToTop && !scrollableState.isScrollInProgress) {
        if (shouldScrollToTop && !scrollableState.isScrollInProgress) {
            if (!isInstrumentedTest()) {
                scrollableState.scrollToItem(0)
            }
            onScrollToTop()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value(),
        onRefresh = onRefresh,
        refreshThreshold = 40.dp,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .applyIf(enableSwipeRefresh, ifTrue = { pullRefresh(pullRefreshState) })
    ) {
        if (isProcessingSearch == IsProcessingSearchState.Loading) {
            Loading(Modifier.fillMaxSize())
        } else if (isLoading == IsLoadingState.Loading) {
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(PLACEHOLDER_ELEMENTS) {
                    PlaceholderItemRow()
                }
            }
        } else if (items.isNotEmpty() || forceContent) {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollableState) {
                header()
                items.forEach { (key, value) ->
                    stickyItemListHeader(key)
                    items(items = value, key = { it.id.id }) { item ->
                        val share = shares[item.shareId]
                        val permissions = share?.role?.toPermissions()
                        val isSelectable = permissions?.canCreate() ?: false
                        val icon = share?.takeIf { !isShareSelected }?.icon
                        val selection = remember(isInSelectionMode, selectedItemIds) {
                            val isSelected = item.shareId to item.id in selectedItemIds
                            ItemSelectionModeState.fromValues(
                                inSelectionMode = isInSelectionMode,
                                isSelected = isSelected,
                                isSelectable = isSelectable
                            )
                        }
                        ActionableItemRow(
                            item = item,
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
                }
                footer()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (forceShowHeader) {
                    header()
                }
                item { emptyContent() }
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing.value(),
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
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
