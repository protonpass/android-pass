package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.autofill.service.R
import me.proton.core.pass.presentation.components.common.item.ItemsList
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    state: SelectItemUiState,
    snackbarHost: @Composable (SnackbarHostState) -> Unit,
    onItemClicked: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost
    ) { padding ->
        when (state.isLoading) {
            IsLoadingState.Loading -> LoadingDialog()
            IsLoadingState.NotLoading -> {
                ItemsList(
                    modifier = modifier.padding(padding),
                    items = state.items,
                    emptyListMessage = R.string.error_credentials_not_found,
                    onRefresh = onRefresh,
                    isRefreshing = state.isRefreshing,
                    onItemClick = onItemClicked
                )
            }
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
            onRefresh = {},
            snackbarHost = {}
        )
    }
}
