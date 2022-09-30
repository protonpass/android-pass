package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.presentation.components.common.item.ItemsList
import me.proton.core.pass.presentation.components.model.ItemUiModel

const val SELECT_ITEM_ROUTE = "autofill/item"


@Composable
fun SelectItemScreen(
    onItemSelected: (AutofillItem) -> Unit
) {
    val viewModel: SelectItemViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    SelectItemScreenView(
        uiState,
        onItemClicked = { viewModel.onItemClicked(it) },
        onItemSelected
    )
}

@Composable
internal fun SelectItemScreenView(
    state: SelectItemUiState,
    onItemClicked: (ItemUiModel) -> Unit,
    onItemSelected: (AutofillItem) -> Unit
) {
    when (state) {
        is SelectItemUiState.Loading -> LoadingDialog()
        is SelectItemUiState.Error -> Text("Something went kaboom: ${state.message}")
        is SelectItemUiState.Selected -> onItemSelected(state.autofillItem)
        is SelectItemUiState.Content -> SelectItemScreenContent(
            state.items,
            onItemClicked = onItemClicked
        )
    }
}

@Composable
internal fun SelectItemScreenContent(
    items: List<ItemUiModel>,
    onItemClicked: (ItemUiModel) -> Unit
) {
    ItemsList(
        items = items,
        onItemClick = onItemClicked
    )
}
