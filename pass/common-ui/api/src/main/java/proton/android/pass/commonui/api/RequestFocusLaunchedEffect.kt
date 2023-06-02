package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

@Composable
fun RequestFocusLaunchedEffect(
    focusRequester: FocusRequester,
    requestFocus: Boolean = true,
    callback: () -> Unit = {}
) {
    if (requestFocus) {
        OneTimeLaunchedEffect(true) {
            delay(DELAY_BEFORE_FOCUS_MS)
            focusRequester.requestFocus()
            callback()
        }
    }
}

private const val DELAY_BEFORE_FOCUS_MS = 200L
