package proton.android.pass.data.impl.usecases

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import proton.android.pass.data.api.usecases.SendUserAccess
import proton.android.pass.data.impl.work.UserAccessWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

class SendUserAccessImpl @Inject constructor(
    private val workManager: WorkManager
) : SendUserAccess {
    override fun invoke() {
        val initialDelaySeconds = Random.nextInt(1, 10).minutes.inWholeSeconds
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
                .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                .build()
        workManager.enqueueUniquePeriodicWork(
            UserAccessWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
