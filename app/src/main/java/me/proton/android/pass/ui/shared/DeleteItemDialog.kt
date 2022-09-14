package me.proton.android.pass.ui.shared

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import me.proton.core.pass.presentation.components.model.ItemUiModel

@Composable
fun ConfirmItemDeletionDialog(
    itemState: MutableState<ItemUiModel?>,
    @StringRes title: Int,
    @StringRes message: Int,
    onConfirm: (ItemUiModel) -> Unit
) {
    val item = itemState.value ?: return
    ConfirmDialog(
        title = stringResource(title),
        message = stringResource(message, item.name),
        state = itemState,
        onConfirm = onConfirm
    )
}
