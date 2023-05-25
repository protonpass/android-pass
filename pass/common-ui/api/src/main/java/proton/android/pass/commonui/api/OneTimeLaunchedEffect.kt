package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope

@Composable
fun OneTimeLaunchedEffect(key1: Any?, block: suspend CoroutineScope.() -> Unit) {
    var executed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1) {
        if (!executed) {
            block()
            executed = true
        }
    }
}
