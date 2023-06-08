package proton.android.pass.data.impl.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.impl.sync.SyncWorker.Companion.WORKER_UNIQUE_NAME
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class SyncManagerImpl @Inject constructor(
    private val scopeProvider: CoroutineScopeProvider,
    private val workManager: WorkManager,
    private val applyPendingEvents: ApplyPendingEvents,
    appLifecycleProvider: AppLifecycleProvider,
    accountManager: AccountManager
) : SyncManager {

    private val state: Flow<SyncState> = combine(
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
                    when (it.appLifecycle) {
                        AppLifecycleProvider.State.Foreground -> {
                            workManager.cancelUniqueWork(WORKER_UNIQUE_NAME)
                            while (currentCoroutineContext().isActive) {
                                runCatching {
                                    applyPendingEvents()
                                }.onFailure { t ->
                                    PassLogger.w(TAG, t, "Apply pending events error")
                                }
                                delay(initialDelay)
                            }
                        }

                        AppLifecycleProvider.State.Background -> enqueueWorker(initialDelay)
                    }
                } else {
                    workManager.cancelUniqueWork(WORKER_UNIQUE_NAME)
                    PassLogger.i(TAG, "$WORKER_UNIQUE_NAME cancelled")
                }
            }
        }
    }

    private fun enqueueWorker(initialDelay: Duration) {
        val request = SyncWorker.getRequestFor(initialDelay)
        workManager.enqueueUniquePeriodicWork(
            WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        PassLogger.i(TAG, "$WORKER_UNIQUE_NAME enqueued")
    }

    companion object {
        private const val TAG = "SyncManagerImpl"
    }
}
