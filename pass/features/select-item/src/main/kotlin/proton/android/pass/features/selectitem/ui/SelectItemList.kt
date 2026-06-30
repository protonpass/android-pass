/*
 * Copyright (c) 2023-2026 Proton AG
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import kotlinx.collections.immutable.toPersistentMap
import proton.android.pass.commonui.api.HomeListItem
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.EmptyList
import proton.android.pass.composecomponents.impl.item.EmptySearchResults
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.item.ItemsListPaging
import proton.android.pass.composecomponents.impl.item.header.ItemCount
import proton.android.pass.composecomponents.impl.item.header.ItemListHeader
import proton.android.pass.composecomponents.impl.item.header.SortingButton
import proton.android.pass.features.selectitem.R
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.searchoptions.api.SearchFilterType

@Composable
internal fun SelectItemList(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    pagingItems: LazyPagingItems<HomeListItem>,
    scrollState: LazyListState = rememberLazyListState(),
    onScrolledToTop: () -> Unit,
    onItemClicked: (ItemUiModel, Boolean) -> Unit,
    onItemOptionsClicked: (ItemUiModel) -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit,
    onEvent: (SelectItemEvent) -> Unit
) {
    val searchUiState = uiState.searchUiState
    val listUiState = uiState.listUiState
    val pinningUiState = uiState.pinningUiState

    val accounts = remember(uiState.listUiState.accountSwitchState.accountList) {
        uiState.listUiState.accountSwitchState.accountList.associate { it.userId to it.email }.toPersistentMap()
    }
    val showAutosaveBanner = listUiState.showAutosaveBanner &&
        listUiState.autosaveMatchCount > 0 &&
        (!searchUiState.inSearchMode || listUiState.items.items.isNotEmpty())

    if (uiState.isPaginationEnabled) {
        ItemsListPaging(
            modifier = modifier,
            pagingItems = pagingItems,
            shares = listUiState.shares,
            scrollableState = scrollState,
            shouldScrollToTop = uiState.listUiState.shouldScrollToTop,
            highlight = searchUiState.searchQuery,
            isRefreshing = listUiState.isRefreshing,
            showMenuIcon = !showAutosaveBanner,
            accounts = accounts,
            header = {
                if (!pinningUiState.inPinningMode) {
                    SelectItemListHeader(
                        suggestionsForTitle = listUiState.items.suggestionsForTitle,
                        suggestions = listUiState.items.suggestions,
                        canLoadExternalImages = listUiState.canLoadExternalImages,
                        showUpgradeMessage = listUiState.displayOnlyPrimaryVaultMessage,
                        showAutosaveBanner = showAutosaveBanner,
                        autosaveMatchCount = listUiState.autosaveMatchCount,
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
                    val loadedItemCount = pagingItems.itemSnapshotList.items
                        .count { it is HomeListItem.Item }
                    // Autosave filters the list client-side, so its real count is the loaded one;
                    // every other mode uses the index-backed total from the ViewModel.
                    val displayItemCount = if (listUiState.showAutosaveBanner) {
                        loadedItemCount
                    } else {
                        listUiState.paginatedItemCount
                    }
                    val shouldShowItemListHeader = loadedItemCount > 0
                    if (shouldShowItemListHeader) {
                        ItemListHeader(
                            countContent = {
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
                                    itemCount = displayItemCount,
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
            },
            onRefresh = {},
            onItemClick = { onItemClicked(it, false) },
            onItemMenuClick = onItemOptionsClicked,
            onScrollToTop = onScrolledToTop,
            canLoadExternalImages = listUiState.canLoadExternalImages,
            emptyContent = {
                if (searchUiState.inSearchMode) {
                    EmptySearchResults()
                } else {
                    EmptyList(
                        emptyListMessage = stringResource(
                            id = if (listUiState.showAutosaveBanner && listUiState.autosaveMatchCount == 0) {
                                R.string.select_item_autosave_no_match
                            } else {
                                R.string.error_credentials_not_found
                            }
                        ),
                        canCreate = !listUiState.showAutosaveBanner,
                        onCreateItemClick = {
                            if (!listUiState.hasVaults) {
                                onEvent(SelectItemEvent.NoVaultsAvailable)
                            } else if (listUiState.accountSwitchState.hasMultipleAccounts) {
                                onNavigate(SelectItemNavigation.SelectAccount)
                            } else {
                                onNavigate(SelectItemNavigation.AddItem)
                            }
                        }
                    )
                }
            }
        )
    } else {
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
            showMenuIcon = !showAutosaveBanner,
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
                        emptyListMessage = stringResource(
                            id = if (listUiState.showAutosaveBanner && listUiState.autosaveMatchCount == 0) {
                                R.string.select_item_autosave_no_match
                            } else {
                                R.string.error_credentials_not_found
                            }
                        ),
                        canCreate = !listUiState.showAutosaveBanner,
                        onCreateItemClick = {
                            if (!listUiState.hasVaults) {
                                onEvent(SelectItemEvent.NoVaultsAvailable)
                            } else if (listUiState.accountSwitchState.hasMultipleAccounts) {
                                onNavigate(SelectItemNavigation.SelectAccount)
                            } else {
                                onNavigate(SelectItemNavigation.AddItem)
                            }
                        }
                    )
                }
            },
            forceContent = listUiState.items.suggestions.isNotEmpty() ||
                listUiState.displayOnlyPrimaryVaultMessage ||
                showAutosaveBanner,
            header = {
                if (!pinningUiState.inPinningMode) {
                    SelectItemListHeader(
                        suggestionsForTitle = listUiState.items.suggestionsForTitle,
                        suggestions = listUiState.items.suggestions,
                        canLoadExternalImages = listUiState.canLoadExternalImages,
                        showUpgradeMessage = listUiState.displayOnlyPrimaryVaultMessage,
                        showAutosaveBanner = showAutosaveBanner,
                        autosaveMatchCount = listUiState.autosaveMatchCount,
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
}
