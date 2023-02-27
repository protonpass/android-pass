package proton.android.pass.data.impl.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import kotlin.time.Duration

class SyncManagerImpl @Inject constructor(
    private val scopeProvider: CoroutineScopeProvider,
    private val workManager: WorkManager,
    appLifecycleProvider: AppLifecycleProvider,
    accountManager: AccountManager
) : SyncManager {

    private val state = combine(
        accountManager.getPrimaryUserId(),
        appLifecycleProvider.state
    ) { userId, appLifecycle ->
        SyncState(userId != null, appLifecycle)
    }

    data class SyncState(
        val isLoggedIn: Boolean,
        val appLifecycle: AppLifecycleProvider.State
    )

    override fun start() {
        PassLogger.i(TAG, "SyncManager start")
        scopeProvider.GlobalIOSupervisedScope.launch {
            state.collectLatest {
                if (it.isLoggedIn) {
                    val initialDelay = when (it.appLifecycle) {
                        AppLifecycleProvider.State.Background -> EventWorkerManager.REPEAT_INTERVAL_BACKGROUND
                        AppLifecycleProvider.State.Foreground -> EventWorkerManager.REPEAT_INTERVAL_FOREGROUND
                    }
                    val immediateDuration = Duration.ZERO
                    when (it.appLifecycle) {
                        AppLifecycleProvider.State.Foreground ->
                            while (currentCoroutineContext().isActive) {
                                enqueueWorker(immediateDuration)
                                delay(initialDelay)
                            }
                        AppLifecycleProvider.State.Background -> enqueueWorker(initialDelay)
                    }
                } else {
                    workManager.cancelUniqueWork(SyncWorker.WORKER_UNIQUE_NAME)
                }
            }
        }
    }

    private fun enqueueWorker(initialDelay: Duration) {
        val request = SyncWorker.getRequestFor(initialDelay)
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        private const val TAG = "SyncManagerImpl"
    }
}
