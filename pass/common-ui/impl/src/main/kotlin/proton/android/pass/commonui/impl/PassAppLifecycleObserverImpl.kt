package proton.android.pass.commonui.impl

import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassAppLifecycleObserverImpl internal constructor(
    mainLooper: Looper,
    processLifecycleOwner: LifecycleOwner
) : PassAppLifecycleProvider, DefaultLifecycleObserver {

    private val mutableState = MutableStateFlow(PassAppLifecycleProvider.State.Background)

    override val lifecycle: Lifecycle by lazy {
        processLifecycleOwner.lifecycle
    }

    override val state: StateFlow<PassAppLifecycleProvider.State> = mutableState.asStateFlow()

    init {
        HandlerCompat.createAsync(mainLooper).post {
            lifecycle.addObserver(this)
        }
    }

    @Inject
    constructor() : this(Looper.getMainLooper(), ProcessLifecycleOwner.get())

    override fun onStart(owner: LifecycleOwner) {
        mutableState.tryEmit(PassAppLifecycleProvider.State.Foreground)
    }

    override fun onStop(owner: LifecycleOwner) {
        mutableState.tryEmit(PassAppLifecycleProvider.State.Background)
    }
}
