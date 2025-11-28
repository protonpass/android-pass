package proton.android.pass.autofill.ui.autofill.common

import android.os.Build
import android.view.MotionEvent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.consumeObscuredTouches(): Modifier = pointerInteropFilter { event ->
    val isObscured = event.flags and MotionEvent.FLAG_WINDOW_IS_OBSCURED != 0
    val isPartiallyObscured = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        event.flags and MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED != 0
    isObscured || isPartiallyObscured
}
