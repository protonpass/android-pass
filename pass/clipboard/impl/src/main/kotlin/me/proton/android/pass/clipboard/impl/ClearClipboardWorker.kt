package me.proton.android.pass.clipboard.impl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.android.pass.log.PassLogger

@HiltWorker
class ClearClipboardWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting doWork")

        val clipboardManager = applicationContext.getSystemService(ClipboardManager::class.java)
        if (clipboardManager == null) {
            PassLogger.i(TAG, "Could not get ClipboardManager")
            return Result.failure()
        }

        clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))

        PassLogger.i(TAG, "Successfully cleared clipboard")
        return Result.success()
    }

    companion object {
        private const val TAG = "ClearClipboardWorker"
    }
}
