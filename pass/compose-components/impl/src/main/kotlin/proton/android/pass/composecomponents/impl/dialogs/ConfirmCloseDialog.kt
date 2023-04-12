package proton.android.pass.composecomponents.impl.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.R

@Composable
fun ConfirmCloseDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    ConfirmDialog(
        modifier = modifier,
        title = stringResource(R.string.confirm_close_dialog_title),
        message = stringResource(R.string.confirm_close_dialog_message),
        confirmText = stringResource(R.string.confirm_close_dialog_close_button),
        state = true,
        onDismiss = onCancel,
        onConfirm = { onConfirm() }
    )
}
