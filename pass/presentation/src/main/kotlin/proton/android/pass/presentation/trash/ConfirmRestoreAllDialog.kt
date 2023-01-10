package proton.android.pass.presentation.trash

import androidx.compose.material.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
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

@Preview
@Composable
fun ConfirmRestoreAllDialogPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            ConfirmRestoreAllDialog(
                show = true,
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}
