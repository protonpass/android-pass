package proton.android.pass.autofill.ui.autofill.select

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

@Composable
fun SelectItemList(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    onScrollToTop: () -> Unit,
    onItemClicked: (ItemUiModel) -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit,
) {
    val searchUiState = uiState.searchUiState
    val listUiState = uiState.listUiState

    ItemsList(
        modifier = modifier,
        items = listUiState.items.items,
        shares = listUiState.shares,
        shouldScrollToTop = uiState.listUiState.shouldScrollToTop,
        highlight = searchUiState.searchQuery,
        isLoading = listUiState.isLoading,
        isProcessingSearch = searchUiState.isProcessingSearch,
        isRefreshing = listUiState.isRefreshing,
        showMenuIcon = false,
        enableSwipeRefresh = false,
        onRefresh = {},
        onItemClick = onItemClicked,
        onItemMenuClick = {},
        onScrollToTop = onScrollToTop,
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
        header = {
            SelectItemListHeader(
                suggestionsForTitle = listUiState.items.suggestionsForTitle,
                suggestions = listUiState.items.suggestions,
                onItemClicked = onItemClicked
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
                                    onNavigate(SelectItemNavigation.SortingBottomsheet(uiState.listUiState.sortingType))
                                }
                            )
                        }
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
                onScrollToTop = {},
                onNavigate = {}
            )
        }
    }
}
