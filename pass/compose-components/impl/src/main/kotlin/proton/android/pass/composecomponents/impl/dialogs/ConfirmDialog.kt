package proton.android.pass.composecomponents.impl.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun <T> ConfirmDialog(
    title: String,
    message: String,
    state: T?,
    onDismiss: () -> Unit,
    onConfirm: (T) -> Unit
) {
    val value = state ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(value)
                onDismiss()
            }) {
                Text(text = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_cancel))
            }
        }
    )
}
