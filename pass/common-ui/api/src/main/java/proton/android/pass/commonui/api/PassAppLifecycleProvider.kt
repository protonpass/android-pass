package proton.android.pass.commonui.api

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.StateFlow

interface PassAppLifecycleProvider {
    val lifecycle: Lifecycle
    val state: StateFlow<State>

    enum class State {
        Foreground,
        Background
    }
}
