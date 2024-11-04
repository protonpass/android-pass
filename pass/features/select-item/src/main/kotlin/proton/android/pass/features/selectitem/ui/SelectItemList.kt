/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.selectitem.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentMap
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.EmptyList
import proton.android.pass.composecomponents.impl.item.EmptySearchResults
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.item.header.ItemCount
import proton.android.pass.composecomponents.impl.item.header.ItemListHeader
import proton.android.pass.composecomponents.impl.item.header.SortingButton
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.features.selectitem.R
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.previewproviders.SelectItemUiStatePreviewProvider

@Composable
fun SelectItemList(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    scrollState: LazyListState = rememberLazyListState(),
    onScrolledToTop: () -> Unit,
    onItemClicked: (ItemUiModel, Boolean) -> Unit,
    onItemOptionsClicked: (ItemUiModel) -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    val searchUiState = uiState.searchUiState
    val listUiState = uiState.listUiState
    val pinningUiState = uiState.pinningUiState

    val items = if (!uiState.pinningUiState.inPinningMode) {
        listUiState.items.items
    } else {
        pinningUiState.filteredItems
    }
    val listItemCount = remember(uiState.listUiState.items) {
        uiState.listUiState.itemCount
    }
    val pinningItemsCount = remember(uiState.pinningUiState.filteredItems) {
        uiState.pinningUiState.itemCount
    }
    val accounts = remember(uiState.listUiState.accountSwitchState.accountList) {
        uiState.listUiState.accountSwitchState.accountList.associate { it.userId to it.email }.toPersistentMap()
    }

    ItemsList(
        modifier = modifier,
        scrollableState = scrollState,
        items = items,
        shares = listUiState.shares,
        shouldScrollToTop = uiState.listUiState.shouldScrollToTop,
        accounts = accounts,
        highlight = searchUiState.searchQuery,
        isLoading = listUiState.isLoading,
        isProcessingSearch = searchUiState.isProcessingSearch,
        isRefreshing = listUiState.isRefreshing,
        showMenuIcon = true,
        enableSwipeRefresh = false,
        canLoadExternalImages = listUiState.canLoadExternalImages,
        onRefresh = {},
        onItemClick = {
            onItemClicked(it, false)
        },
        onItemMenuClick = onItemOptionsClicked,
        onScrollToTop = onScrolledToTop,
        emptyContent = {
            if (searchUiState.inSearchMode) {
                EmptySearchResults()
            } else {
                EmptyList(
                    emptyListMessage = stringResource(id = R.string.error_credentials_not_found),
                    onCreateItemClick = {
                        if (listUiState.accountSwitchState.accountList.size > 1) {
                            onNavigate(SelectItemNavigation.SelectAccount)
                        } else {
                            onNavigate(SelectItemNavigation.AddItem)
                        }
                    }
                )
            }
        },
        forceContent = listUiState.items.suggestions.isNotEmpty() || listUiState.displayOnlyPrimaryVaultMessage,
        header = {
            if (!pinningUiState.inPinningMode) {
                SelectItemListHeader(
                    suggestionsForTitle = listUiState.items.suggestionsForTitle,
                    suggestions = listUiState.items.suggestions,
                    canLoadExternalImages = listUiState.canLoadExternalImages,
                    showUpgradeMessage = listUiState.displayOnlyPrimaryVaultMessage,
                    canUpgrade = listUiState.canUpgrade,
                    accounts = accounts,
                    onItemOptionsClicked = onItemOptionsClicked,
                    onItemClicked = {
                        onItemClicked(it, true)
                    },
                    onUpgradeClick = { onNavigate(SelectItemNavigation.Upgrade) }
                )
            }
            item {
                val shouldShowItemListHeader = remember(uiState) {
                    uiState.shouldShowItemListHeader()
                }
                if (shouldShowItemListHeader) {
                    ItemListHeader(
                        countContent = {
                            val count = if (uiState.pinningUiState.inPinningMode) {
                                pinningItemsCount
                            } else {
                                listItemCount
                            }
                            ItemCount(
                                modifier = Modifier.padding(
                                    start = Spacing.medium,
                                    top = Spacing.none,
                                    end = Spacing.none,
                                    bottom = Spacing.none
                                ),
                                showSearchResults = uiState.searchUiState.inSearchMode &&
                                    uiState.searchUiState.searchQuery.isNotEmpty(),
                                itemType = SearchFilterType.All,
                                itemCount = count.takeIf { !uiState.searchUiState.isProcessingSearch.value() },
                                isPinnedMode = uiState.pinningUiState.inPinningMode
                            )
                        },
                        sortingContent = {
                            SortingButton(
                                sortingType = uiState.listUiState.sortingType,
                                onSortingOptionsClick = {
                                    onNavigate(SelectItemNavigation.SortingBottomsheet)
                                }
                            )
                        }
                    )
                }
            }
        }
    )
}

class ThemeAndSelectItemUiStateProvider :
    ThemePairPreviewProvider<SelectItemUiState>(SelectItemUiStatePreviewProvider())

@Preview
@Composable
fun SelectItemListPreview(
    @PreviewParameter(ThemeAndSelectItemUiStateProvider::class) input: Pair<Boolean, SelectItemUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectItemList(
                uiState = input.second,
                onItemClicked = { _, _ -> },
                onItemOptionsClicked = {},
                onScrolledToTop = {},
                onNavigate = {}
            )
        }
    }
}
