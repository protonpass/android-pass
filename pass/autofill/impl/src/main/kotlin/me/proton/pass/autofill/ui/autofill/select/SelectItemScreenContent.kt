package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.composecomponents.impl.buttons.PassFloatingActionButton

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    onItemClicked: (ItemUiModel) -> Unit,
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
        SelectItemList(
            modifier = modifier.padding(padding),
            uiState = uiState,
            onItemClicked = onItemClicked
        )
    }
}
