package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.presentation.components.common.item.ItemsList
import me.proton.core.pass.presentation.components.model.ItemUiModel

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    state: SelectItemUiState,
    onItemClicked: (ItemUiModel) -> Unit,
    onItemSelected: (AutofillItem) -> Unit
) {
    Surface(modifier = modifier) {
        when (state) {
            is SelectItemUiState.Loading -> LoadingDialog()
            is SelectItemUiState.Error -> Text("Something went kaboom: ${state.message}")
            is SelectItemUiState.Selected -> onItemSelected(state.autofillItem)
            is SelectItemUiState.Content -> ItemsList(
                items = state.items,
                onItemClick = onItemClicked
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewSelectItemScreenContent(
    @PreviewParameter(SelectItemUiStatePreviewProvider::class) state: SelectItemUiState
) {
    ProtonTheme {
        SelectItemScreenContent(
            state = state,
            onItemClicked = {},
            onItemSelected = {}
        )
    }
}
