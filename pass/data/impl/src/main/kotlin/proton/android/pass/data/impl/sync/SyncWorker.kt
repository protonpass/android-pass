package proton.android.pass.data.impl.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

@HiltWorker
open class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val applyPendingEvents: ApplyPendingEvents
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting sync worker")
        return runCatching {
            applyPendingEvents()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                PassLogger.w(TAG, it, "Sync worker error")
                Result.failure()
            }
        )
    }

    companion object {
        private const val TAG = "SyncWorker"

        const val WORKER_UNIQUE_NAME = "sync_worker"

        fun getRequestFor(initialDelay: Duration): PeriodicWorkRequest {
            val initialDelaySeconds = initialDelay.inWholeSeconds
            val backoffDelaySeconds = EventWorkerManager.BACKOFF_DELAY.inWholeSeconds
            val repeatIntervalSeconds = EventWorkerManager.REPEAT_INTERVAL_BACKGROUND.inWholeSeconds
            return PeriodicWorkRequestBuilder<SyncWorker>(repeatIntervalSeconds, TimeUnit.SECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    backoffDelaySeconds,
                    TimeUnit.SECONDS
                )
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                .build()
        }
    }
}
