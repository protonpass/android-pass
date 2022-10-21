package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.domain.entity.PackageName

const val SELECT_ITEM_ROUTE = "autofill/item"

@Composable
fun SelectItemScreen(
    modifier: Modifier = Modifier,
    packageName: PackageName,
    onItemSelected: (AutofillItem) -> Unit,
    viewModel: SelectItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.itemClickedEvent is ItemClickedEvent.Clicked) {
        (uiState.itemClickedEvent as? ItemClickedEvent.Clicked)?.let {
            onItemSelected(it.item)
        }
    }

    SelectItemScreenContent(
        modifier = modifier,
        uiState = uiState,
        onItemClicked = { viewModel.onItemClicked(it, packageName) },
        onRefresh = { viewModel.onRefresh() }
    )
}
