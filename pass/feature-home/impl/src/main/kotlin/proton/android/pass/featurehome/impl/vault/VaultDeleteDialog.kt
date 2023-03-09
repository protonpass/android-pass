package proton.android.pass.featurehome.impl.vault

import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headline
import proton.android.pass.featurehome.impl.R

@Composable
fun VaultDeleteDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    vaultName: String?,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show || vaultName == null) return

    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.vault_delete_dialog_title),
                style = ProtonTheme.typography.headline,
                color = ProtonTheme.colors.textNorm
            )
        },
        text = {
            ProtonAlertDialogText(
                text = stringResource(
                    id = R.string.vault_delete_dialog_body,
                    formatArgs = arrayOf(vaultName)
                )
            )

        },
        confirmButton = {
            ProtonTextButton(
                onClick = { onDelete() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = ProtonTheme.colors.notificationError
                )
            ) {
                Text(
                    text = stringResource(R.string.vault_delete_dialog_delete_action),
                    color = ProtonTheme.colors.notificationError,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        dismissButton = {
            ProtonTextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.vault_delete_dialog_cancel_action),
                    color = ProtonTheme.colors.brandNorm,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}
