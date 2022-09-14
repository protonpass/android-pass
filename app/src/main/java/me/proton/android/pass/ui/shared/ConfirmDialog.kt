package me.proton.android.pass.ui.shared

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.R

@Composable
fun <T> ConfirmDialog(
    title: String,
    message: String,
    state: MutableState<T?>,
    onConfirm: (T) -> Unit
) {
    val value = state.value ?: return

    AlertDialog(
        onDismissRequest = { state.value = null },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(value)
                state.value = null
            }) {
                Text(text = stringResource(id = R.string.presentation_alert_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { state.value = null }) {
                Text(text = stringResource(id = R.string.presentation_alert_cancel))
            }
        }
    )
}
