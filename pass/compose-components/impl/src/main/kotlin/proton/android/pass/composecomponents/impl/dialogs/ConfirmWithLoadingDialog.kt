package proton.android.pass.composecomponents.impl.dialogs

import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headline

@Composable
fun ConfirmWithLoadingDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    isLoading: Boolean,
    isConfirmActionDestructive: Boolean,
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (!show) return

    val confirmColor = if (isConfirmActionDestructive) {
        ProtonTheme.colors.notificationError
    } else {
        ProtonTheme.colors.brandNorm
    }
    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = ProtonTheme.typography.headline,
                color = ProtonTheme.colors.textNorm
            )
        },
        text = {
            ProtonAlertDialogText(
                text = message
            )
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                ProtonTextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = confirmColor
                    )
                ) {
                    Text(
                        text = confirmText,
                        color = confirmColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                ProtonTextButton(onClick = onCancel) {
                    Text(
                        text = cancelText,
                        color = ProtonTheme.colors.brandNorm,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}
