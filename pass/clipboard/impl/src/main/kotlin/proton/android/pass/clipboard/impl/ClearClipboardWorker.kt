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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import proton.android.pass.log.api.PassLogger

@HiltWorker
class ClearClipboardWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val keyStoreCrypto: KeyStoreCrypto
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting doWork")

        val encryptedExpected = workerParameters.inputData.getString(EXPECTED_CONTENTS_KEY)
        if (encryptedExpected == null) {
            PassLogger.i(TAG, "Could not get expected clipboard contents")
            return Result.failure()
        }
        val expected = encryptedExpected.decrypt(keyStoreCrypto)

        val clipboardManager = applicationContext.getSystemService(ClipboardManager::class.java)
        if (clipboardManager == null) {
            PassLogger.i(TAG, "Could not get ClipboardManager")
            return Result.failure()
        }

        val primaryClip = clipboardManager.primaryClip
        if (primaryClip != null) {
            if (primaryClip.itemCount > 0) {
                val currentContents = primaryClip.getItemAt(0)
                if (currentContents.text == expected) {
                    clearClipboard(clipboardManager)
                    PassLogger.i(TAG, "Clipboard did not have previous contents")
                } else {
                    PassLogger.i(
                        TAG,
                        "Did not clear clipboard as it did not have the expected contents"
                    )
                }
            }
        } else {
            PassLogger.i(TAG, "Clipboard did not have contents")
        }

        PassLogger.i(TAG, "Successfully cleared clipboard")
        return Result.success()
    }

    private fun clearClipboard(clipboardManager: ClipboardManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboardManager.clearPrimaryClip()
        } else {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }


    companion object {
        private const val TAG = "ClearClipboardWorker"
        private const val EXPECTED_CONTENTS_KEY = "expected_contents"

        fun createInputData(expectedClipboardContents: EncryptedString): Data =
            Data.Builder()
                .putString(EXPECTED_CONTENTS_KEY, expectedClipboardContents)
                .build()

    }
}
