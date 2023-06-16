/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
