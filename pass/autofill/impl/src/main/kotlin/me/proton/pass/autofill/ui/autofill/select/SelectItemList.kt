package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.autofill.service.R
import me.proton.pass.autofill.ui.previewproviders.SelectItemUiStatePreviewProvider
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.components.common.EmptySearchResults
import me.proton.pass.presentation.components.common.item.EmptyList
import me.proton.pass.presentation.components.common.item.ItemsList
import me.proton.pass.presentation.components.model.ItemUiModel

@Composable
fun SelectItemList(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    onItemClicked: (ItemUiModel) -> Unit
) {
    val searchUiState = uiState.searchUiState
    val listUiState = uiState.listUiState

    ItemsList(
        modifier = modifier,
        items = listUiState.items.items,
        shouldScrollToTop = false,
        highlight = searchUiState.searchQuery,
        isLoading = listUiState.isLoading,
        isProcessingSearch = searchUiState.isProcessingSearch,
        isRefreshing = listUiState.isRefreshing,
        showMenuIcon = false,
        enableSwipeRefresh = false,
        onRefresh = {},
        onItemClick = onItemClicked,
        onItemMenuClick = {},
        onScrollToTop = {},
        emptyContent = {
            if (searchUiState.inSearchMode) {
                EmptySearchResults()
            } else {
                EmptyList(emptyListMessage = stringResource(id = R.string.error_credentials_not_found))
            }
        },
        header = {
            SelectItemListHeader(
                suggestionsForTitle = listUiState.items.suggestionsForTitle,
                suggestions = listUiState.items.suggestions,
                onItemClicked = onItemClicked
            )
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
    ProtonTheme(isDark = input.first) {
        Surface {
            SelectItemList(
                uiState = input.second,
                onItemClicked = {}
            )
        }
    }
}
