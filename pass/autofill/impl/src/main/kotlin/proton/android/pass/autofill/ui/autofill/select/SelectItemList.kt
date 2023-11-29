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

package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.navigation.SelectItemNavigation
import proton.android.pass.autofill.ui.previewproviders.SelectItemUiStatePreviewProvider
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.EmptyList
import proton.android.pass.composecomponents.impl.item.EmptySearchResults
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.item.header.ItemListHeader
import proton.android.pass.composecomponents.impl.item.header.SortingButton
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featuresearchoptions.api.SearchFilterType

@Composable
fun SelectItemList(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    scrollState: LazyListState = rememberLazyListState(),
    onScrolledToTop: () -> Unit,
    onItemClicked: (ItemUiModel) -> Unit,
    onItemOptionsClicked: (ItemUiModel) -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit,
) {
    val searchUiState = uiState.searchUiState
    val listUiState = uiState.listUiState

    ItemsList(
        modifier = modifier,
        scrollableState = scrollState,
        items = listUiState.items.items,
        shares = listUiState.shares,
        shouldScrollToTop = uiState.listUiState.shouldScrollToTop,
        highlight = searchUiState.searchQuery,
        isLoading = listUiState.isLoading,
        isProcessingSearch = searchUiState.isProcessingSearch,
        isRefreshing = listUiState.isRefreshing,
        showMenuIcon = true,
        enableSwipeRefresh = false,
        canLoadExternalImages = listUiState.canLoadExternalImages,
        onRefresh = {},
        onItemClick = onItemClicked,
        onItemMenuClick = onItemOptionsClicked,
        onScrollToTop = onScrolledToTop,
        emptyContent = {
            if (searchUiState.inSearchMode) {
                EmptySearchResults()
            } else {
                EmptyList(
                    emptyListMessage = stringResource(id = R.string.error_credentials_not_found),
                    onCreateItemClick = { onNavigate(SelectItemNavigation.AddItem) }
                )
            }
        },
        forceContent = listUiState.items.suggestions.isNotEmpty() || listUiState.displayOnlyPrimaryVaultMessage,
        header = {
            SelectItemListHeader(
                suggestionsForTitle = listUiState.items.suggestionsForTitle,
                suggestions = listUiState.items.suggestions,
                canLoadExternalImages = listUiState.canLoadExternalImages,
                showUpgradeMessage = listUiState.displayOnlyPrimaryVaultMessage,
                canUpgrade = listUiState.canUpgrade,
                onItemOptionsClicked = onItemOptionsClicked,
                onItemClicked = onItemClicked,
                onUpgradeClick = { onNavigate(SelectItemNavigation.Upgrade) }
            )
            item {
                if (shouldShowItemListHeader(uiState)) {
                    val count = remember(uiState.listUiState.items) {
                        uiState.listUiState.items.items.map { it.items }.flatten().count() +
                            uiState.listUiState.items.suggestions.count()
                    }
                    ItemListHeader(
                        showSearchResults = uiState.searchUiState.inSearchMode &&
                            uiState.searchUiState.searchQuery.isNotEmpty(),
                        itemCount = count.takeIf { !uiState.searchUiState.isProcessingSearch.value() },
                        sortingContent = {
                            SortingButton(
                                sortingType = uiState.listUiState.sortingType,
                                onSortingOptionsClick = {
                                    onNavigate(SelectItemNavigation.SortingBottomsheet)
                                }
                            )
                        },
                        itemType = SearchFilterType.All
                    )
                }
            }
        }
    )
}

private fun shouldShowItemListHeader(uiState: SelectItemUiState) =
    uiState.listUiState.items.items.isNotEmpty() &&
        uiState.listUiState.isLoading == IsLoadingState.NotLoading &&
        !uiState.searchUiState.isProcessingSearch.value()

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
                onItemClicked = {},
                onItemOptionsClicked = {},
                onScrolledToTop = {},
                onNavigate = {}
            )
        }
    }
}
