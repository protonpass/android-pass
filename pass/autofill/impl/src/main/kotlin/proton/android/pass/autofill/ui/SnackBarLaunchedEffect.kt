package proton.android.pass.autofill.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHostState
import proton.android.pass.notifications.api.SnackbarMessage

@Composable
fun SnackBarLaunchedEffect(
    snackBarMessage: SnackbarMessage?,
    passSnackBarHostState: PassSnackbarHostState,
    onSnackBarMessageDelivered: () -> Unit
) {
    snackBarMessage ?: return
    val snackBarMessageLocale = stringResource(id = snackBarMessage.id)
    LaunchedEffect(snackBarMessage) {
        onSnackBarMessageDelivered()
        passSnackBarHostState.showSnackbar(
            snackBarMessage.type,
            snackBarMessageLocale
        )
    }
}

