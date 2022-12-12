package me.proton.pass.presentation.trash

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.proton.pass.presentation.R

@Composable
internal fun ConfirmRestoreAllDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alert_confirm_restore_all_title)) },
        text = { Text(stringResource(R.string.alert_confirm_restore_all_message)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
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

