package proton.android.pass.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.pass.domain.Item
import me.proton.core.presentation.R

@Composable
fun ConfirmSendToTrashDialog(
    item: Item,
    @StringRes title: Int,
    @StringRes message: Int,
    itemName: String,
    onConfirm: (Item) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message, itemName)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(item)
                onDismiss()
            }) {
                Text(text = stringResource(id = R.string.presentation_alert_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.presentation_alert_cancel))
            }
        }
    )
}
