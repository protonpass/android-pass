package me.proton.android.pass.ui.shared

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.proton.core.pass.presentation.components.model.ItemUiModel

@Composable
fun ConfirmItemDeletionDialog(
    state: ItemUiModel?,
    @StringRes title: Int,
    @StringRes message: Int,
    onDismiss: () -> Unit,
    onConfirm: (ItemUiModel) -> Unit
) {
    val item = state ?: return
    ConfirmDialog(
        title = stringResource(title),
        message = stringResource(message, item.name),
        state = state,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
