package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun Lifecycle.observeAsState(): State<Lifecycle.Event> {
    val state = remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    DisposableEffect(this) {
        val observer = LifecycleEventObserver { _, event ->
            state.value = event
        }
        this@observeAsState.addObserver(observer)
        onDispose {
            this@observeAsState.removeObserver(observer)
        }
    }
    return state
}

@Composable
fun OnResumeCallback(callback: (Boolean) -> Unit) {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.observeAsState()
    OnResumeLaunchedEffect(lifecycleState, callback)
}

@Composable
fun OnResumeLaunchedEffect(lifecycle: Lifecycle.Event, callback: (Boolean) -> Unit) {
    var isFirstTime by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(lifecycle) {
        if (lifecycle == Lifecycle.Event.ON_RESUME) {
            callback(isFirstTime)
        }
        isFirstTime = false
    }
}

