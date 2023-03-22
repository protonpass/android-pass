package proton.android.pass.telemetry.impl.startup

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.util.kotlin.CoroutineScopeProvider
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.telemetry.impl.work.TelemetrySenderWorker
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

interface TelemetryStartupManager {
    fun start()
}

class TelemetryStartupManagerImpl @Inject constructor(
    private val scopeProvider: CoroutineScopeProvider,
    private val workManager: WorkManager,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager
) : TelemetryStartupManager {

    override fun start() {
        PassLogger.i(TAG, "TelemetryStartupManager start")
        scopeProvider.GlobalIOSupervisedScope.launch {
            launch { startListener() }
            startWorker()
        }
    }

    private suspend fun startWorker() {
        accountManager.getPrimaryUserId().collectLatest {
            if (it == null) {
                // User not logged in
                workManager.cancelUniqueWork(TelemetrySenderWorker.WORKER_UNIQUE_NAME)
                PassLogger.i(TAG, "TelemetryWorker cancelled")
            } else {
                enqueueWorker()
                PassLogger.i(TAG, "TelemetryWorker enqueued")
            }
        }
    }

    private fun enqueueWorker() {
        val request = TelemetrySenderWorker.getRequestFor(
            repeatInterval = SEND_TELEMETRY_INTERVAL,
            initialDelay = SEND_TELEMETRY_INITIAL_DELAY
        )
        workManager.enqueueUniquePeriodicWork(
            TelemetrySenderWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    private suspend fun startListener() {
        telemetryManager.startListening(
            onSubscribed = {
                PassLogger.i(TAG, "TelemetryManager ready for receiving events")
            },
            onPerformed = {}
        )
    }

    companion object {
        private const val TAG = "TelemetryStartupManagerImpl"

        private val SEND_TELEMETRY_INTERVAL = 6.hours
        private val SEND_TELEMETRY_INITIAL_DELAY = 30.seconds
    }
}
