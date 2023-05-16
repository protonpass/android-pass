package proton.android.pass.data.impl.usecases

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import proton.android.pass.data.api.usecases.UserPlanWorkerLauncher
import proton.android.pass.data.impl.work.UserAccessWorker
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserPlanWorkerLauncherImpl @Inject constructor(
    private val workManager: WorkManager
) : UserPlanWorkerLauncher {

    override fun start() {
        PassLogger.i(TAG, "Starting UserAccessWorker")
        val backoffDelaySeconds = EventWorkerManager.BACKOFF_DELAY.inWholeSeconds
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<UserAccessWorker>(1, TimeUnit.DAYS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    backoffDelaySeconds,
                    TimeUnit.SECONDS
                )
                .setConstraints(constraints)
                .build()
        workManager.enqueueUniquePeriodicWork(
            UserAccessWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun cancel() {
        PassLogger.i(TAG, "Cancelling UserAccessWorker")
        workManager.cancelUniqueWork(UserAccessWorker.WORKER_UNIQUE_NAME)
    }

    companion object {
        private const val TAG = "UserPlanWorkerLauncherImpl"
    }
}
