package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.autofill.service.R
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.presentation.components.common.item.ItemsList
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    state: SelectItemUiState,
    onItemClicked: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit
) {
    Surface(modifier = modifier) {
        when (state.isLoading) {
            IsLoadingState.Loading -> LoadingDialog()
            IsLoadingState.NotLoading -> {
                ItemsList(
                    items = state.items,
                    emptyListMessage = R.string.error_credentials_not_found,
                    onRefresh = onRefresh,
                    isRefreshing = state.isRefreshing,
                    onItemClick = onItemClicked
                )
            }
        }

        if (state.errorMessage is Some) {
            Text("Something went boom: ${state.errorMessage.value}")
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
            onRefresh = {}
        )
    }
}
