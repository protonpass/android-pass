package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope

@Composable
fun <T> OneTimeLaunchedEffect(
    key: T,
    saver: Saver<T?, out Any> = autoSaver(),
    block: suspend CoroutineScope.() -> Unit
) {
    var oldKey by rememberSaveable(stateSaver = saver) { mutableStateOf(null) }
    if (oldKey != key) {
        LaunchedEffect(key) {
            block()
            oldKey = key
        }
    }
}
