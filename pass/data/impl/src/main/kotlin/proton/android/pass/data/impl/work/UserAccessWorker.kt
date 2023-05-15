package proton.android.pass.data.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import proton.android.pass.data.impl.usecases.SendUserAccessRequest
import proton.android.pass.data.impl.usecases.SendUserAccessResult

@HiltWorker
class UserAccessWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val sendUserAccessRequest: SendUserAccessRequest
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result =
        when (sendUserAccessRequest()) {
            SendUserAccessResult.Retry -> Result.retry()
            SendUserAccessResult.Failure -> Result.failure()
            SendUserAccessResult.Success -> Result.success()
        }

    companion object {
        const val WORKER_UNIQUE_NAME = "user_access_worker"
    }
}
