package me.proton.android.pass.ui.shared

import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.R
import me.proton.core.pass.presentation.components.model.ItemUiModel

@Composable
fun ConfirmItemDeletionDialog(
    itemState: MutableState<ItemUiModel?>,
    @StringRes title: Int,
    @StringRes message: Int,
    onConfirm: (ItemUiModel) -> Unit
) {
    val item = itemState.value ?: return

    AlertDialog(
        onDismissRequest = { itemState.value = null },
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message, item.name)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(item)
                itemState.value = null
            }) {
                Text(text = stringResource(id = R.string.presentation_alert_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { itemState.value = null }) {
                Text(text = stringResource(id = R.string.presentation_alert_cancel))
            }
        }
    )
}
