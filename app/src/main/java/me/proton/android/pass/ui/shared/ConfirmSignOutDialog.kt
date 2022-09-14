package me.proton.android.pass.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.R

@Composable
fun ConfirmSignOutDialog(
    showState: MutableState<Boolean?>,
    onConfirm: () -> Unit
) {
    ConfirmDialog(
        title = stringResource(R.string.alert_confirm_sign_out_title),
        message = stringResource(R.string.alert_confirm_sign_out_message),
        state = showState,
        onConfirm = { onConfirm() }
    )
}
