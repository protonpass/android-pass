package proton.android.pass.data.impl.usecases

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import proton.android.pass.data.api.usecases.SendUserAccess
import proton.android.pass.data.impl.work.UserAccessWorker
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class SendUserAccessImpl @Inject constructor(
    private val workManager: WorkManager
) : SendUserAccess {
    override fun invoke() {
        workManager.enqueue(
            OneTimeWorkRequestBuilder<UserAccessWorker>().build()
        )
        PassLogger.i(TAG, "Scheduled UserAccessWorker")
    }

    companion object {
        private const val TAG = "SendUserAccessImpl"
    }
}
