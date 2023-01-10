package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.EmptyList
import proton.android.pass.composecomponents.impl.item.ItemsList
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.autofill.service.R
import proton.android.pass.autofill.ui.previewproviders.SelectItemUiStatePreviewProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.item.EmptySearchResults

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
