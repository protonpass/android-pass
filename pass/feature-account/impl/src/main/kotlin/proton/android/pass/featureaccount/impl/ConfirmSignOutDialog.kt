package proton.android.pass.featureaccount.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmDialog

@Composable
fun ConfirmSignOutDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    ConfirmDialog(
        title = stringResource(R.string.alert_confirm_sign_out_title),
        message = stringResource(R.string.alert_confirm_sign_out_message),
        state = show,
        onDismiss = onDismiss,
        onConfirm = { onConfirm() }
    )
}
