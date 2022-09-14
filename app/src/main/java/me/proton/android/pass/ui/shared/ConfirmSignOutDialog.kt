package me.proton.android.pass.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.R

@Composable
fun ConfirmSignOutDialog(
    state: Boolean?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmDialog(
        title = stringResource(R.string.alert_confirm_sign_out_title),
        message = stringResource(R.string.alert_confirm_sign_out_message),
        state = state,
        onDismiss = onDismiss,
        onConfirm = { onConfirm() }
    )
}
