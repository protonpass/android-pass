package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.delay

@Composable
fun RequestFocusLaunchedEffect(focusRequester: FocusRequester, requestFocus: Boolean = true) {
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            delay(DELAY_BEFORE_FOCUS_MS)
            focusRequester.requestFocus()
        }
    }
}

private const val DELAY_BEFORE_FOCUS_MS = 200L
