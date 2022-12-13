package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.autofill.service.R
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.components.common.EmptySearchResults
import me.proton.pass.presentation.components.common.PassFloatingActionButton
import me.proton.pass.presentation.components.common.item.EmptyList
import me.proton.pass.presentation.components.common.item.ItemsList
import me.proton.pass.presentation.components.model.ItemUiModel

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    onItemClicked: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onCreateLoginClicked: () -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            PassFloatingActionButton(
                onClick = onCreateLoginClicked
            )
        },
        topBar = {
            SelectItemTopAppBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                onSearchQueryChange = onSearchQueryChange,
                onEnterSearch = onEnterSearch,
                onStopSearching = onStopSearching,
                onClose = onClose
            )
        }
    ) { padding ->
        ItemsList(
            modifier = modifier.padding(padding),
            items = uiState.listUiState.items,
            shouldScrollToTop = false,
            highlight = uiState.searchUiState.searchQuery,
            isLoading = uiState.listUiState.isLoading,
            isProcessingSearch = uiState.searchUiState.isProcessingSearch,
            isRefreshing = uiState.listUiState.isRefreshing,
            onRefresh = onRefresh,
            onItemClick = onItemClicked,
            onItemMenuClick = {},
            onScrollToTop = {},
            emptyContent = {
                if (uiState.searchUiState.inSearchMode) {
                    EmptySearchResults()
                } else {
                    EmptyList(emptyListMessage = stringResource(id = R.string.error_credentials_not_found))
                }
            }
        )
    }
}

class ThemeAndSelectItemUiStateProvider :
    ThemePairPreviewProvider<SelectItemUiState>(SelectItemUiStatePreviewProvider())

@Preview
@Composable
fun PreviewSelectItemScreenContent(
    @PreviewParameter(ThemeAndSelectItemUiStateProvider::class) input: Pair<Boolean, SelectItemUiState>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            SelectItemScreenContent(
                uiState = input.second,
                onItemClicked = {},
                onRefresh = {},
                onSearchQueryChange = {},
                onEnterSearch = {},
                onStopSearching = {},
                onCreateLoginClicked = {},
                onClose = {}
            )
        }
    }
}
