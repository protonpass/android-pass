package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.pass.autofill.service.R
import me.proton.pass.presentation.components.common.EmptySearchResults
import me.proton.pass.presentation.components.common.item.ActionableItemRow
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
            val listItems = listUiState.items
            if (listItems.suggestions.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(
                            R.string.autofill_suggestions_for_placeholder,
                            listItems.suggestionsForTitle
                        ),
                        style = ProtonTheme.typography.defaultSmallWeak
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // As items can appear in both lists, we need to use a different key here
                // so there are not two items with the same key
                items(items = listItems.suggestions, key = { "suggestion-${it.id.id}" }) { item ->
                    ActionableItemRow(
                        item = item,
                        showMenuIcon = false,
                        onItemClick = onItemClicked
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(R.string.autofill_suggestions_other_items),
                        style = ProtonTheme.typography.defaultSmallWeak
                    )
                }
            }
        }
    )
}
