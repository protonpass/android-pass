package proton.android.pass.clipboard.impl

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ClearClipboardScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val keyStoreCrypto: KeyStoreCrypto
) {
    fun schedule(delaySeconds: Long, expectedClipboardContents: String) {
        val data =
            ClearClipboardWorker.createInputData(expectedClipboardContents.encrypt(keyStoreCrypto))
        workManager.enqueue(
            OneTimeWorkRequestBuilder<ClearClipboardWorker>()
                .setInputData(data)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .build()
        )
        PassLogger.i(TAG, "Scheduled ClearClipboardWorker on $delaySeconds seconds")
    }

    companion object {
        private const val TAG = "ClearClipboardScheduler"
    }
}
