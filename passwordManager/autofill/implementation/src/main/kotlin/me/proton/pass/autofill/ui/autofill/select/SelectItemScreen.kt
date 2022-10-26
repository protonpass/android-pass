package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.autofill.entities.AutofillItem

const val SELECT_ITEM_ROUTE = "autofill/item"

@Composable
fun SelectItemScreen(
    modifier: Modifier = Modifier,
    initialState: SelectItemInitialState,
    onItemSelected: (AutofillItem) -> Unit,
    viewModel: SelectItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialState.webDomain) {
        viewModel.setWebDomain(initialState.webDomain)
    }

    LaunchedEffect(uiState.listUiState.itemClickedEvent is ItemClickedEvent.Clicked) {
        (uiState.listUiState.itemClickedEvent as? ItemClickedEvent.Clicked)?.let {
            onItemSelected(it.item)
        }
    }

    SelectItemScreenContent(
        modifier = modifier,
        uiState = uiState,
        onItemClicked = { viewModel.onItemClicked(it, initialState.packageName) },
        onRefresh = { viewModel.onRefresh() },
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onEnterSearch = { viewModel.onEnterSearch() },
        onStopSearching = { viewModel.onStopSearching() }
    )
}
