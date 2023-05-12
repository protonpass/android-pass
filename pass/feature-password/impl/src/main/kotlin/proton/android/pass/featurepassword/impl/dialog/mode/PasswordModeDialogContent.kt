package proton.android.pass.featurepassword.impl.dialog.mode

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.featurepassword.R
import proton.android.pass.preferences.PasswordGenerationMode

@Composable
fun PasswordModeDialogContent(
    modifier: Modifier = Modifier,
    state: PasswordModeUiState,
    onOptionSelected: (PasswordGenerationMode) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.password_type)) },
        text = {
            PasswordModeList(
                options = state.options,
                selected = state.selected,
                onSelected = onOptionSelected
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) {
                Text(text = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_cancel))
            }
        }
    )
}
