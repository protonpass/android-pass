package me.proton.android.pass.clipboard.impl

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import me.proton.android.pass.log.PassLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ClearClipboardScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    fun schedule(delaySeconds: Long) {
        workManager.enqueue(
            OneTimeWorkRequestBuilder<ClearClipboardWorker>()
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .build()
        )
        PassLogger.i(TAG, "Scheduled ClearClipboardWorker on $delaySeconds seconds")
    }

    companion object {
        private const val TAG = "ClearClipboardScheduler"
    }
}
