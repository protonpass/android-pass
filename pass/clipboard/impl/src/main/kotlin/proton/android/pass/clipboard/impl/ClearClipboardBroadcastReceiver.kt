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

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@AndroidEntryPoint
class ClearClipboardBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var encryptionContextProvider: EncryptionContextProvider

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action != INTENT_FILTER) return
        PassLogger.d(TAG, "Broadcast receiver invoked")

        val clipboardManager = context.getSystemService(ClipboardManager::class.java)
        if (clipboardManager == null) {
            PassLogger.w(TAG, "Could not get ClipboardManager")
            return
        }

        val encryptedExpected = intent.getStringExtra(EXPECTED_CONTENTS_KEY)
        if (encryptedExpected == null) {
            PassLogger.w(TAG, "Could not get expected clipboard contents")
            return
        }

        // Check if it has primary clip
        if (shouldClearClipboard(context, clipboardManager, encryptedExpected)) {
            clearClipboard(clipboardManager)
            PassLogger.i(TAG, "Successfully cleared clipboard")
        }
    }

    private fun shouldClearClipboard(
        context: Context,
        clipboardManager: ClipboardManager,
        expected: EncryptedString
    ): Boolean {
        if (clipboardManager.hasPrimaryClip()) {
            if (clipboardManager.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) != true) {
                PassLogger.i(TAG, "Did not clear clipboard as it did not have the expected mime type")
                return false
            }

            val primaryClip = clipboardManager.primaryClip
            if (primaryClip != null) {
                if (primaryClip.itemCount > 0) {
                    val currentContents = primaryClip.getItemAt(0)
                    val decryptedExpected = encryptionContextProvider.withEncryptionContext {
                        decrypt(expected)
                    }

                    val clipboardContents = if (currentContents.text != null) {
                        currentContents.text
                    } else {
                        currentContents.coerceToText(context)
                    }

                    if (clipboardContents == decryptedExpected) {
                        return true
                    } else {
                        PassLogger.i(
                            TAG,
                            "Did not clear clipboard as it did not have the expected contents"
                        )
                    }
                } else {
                    PassLogger.i(TAG, "Did not clear clipboard as ItemCount = 0")
                }
            } else {
                PassLogger.i(TAG, "Could not access the clipboard contents")
            }
        } else {
            PassLogger.i(TAG, "System does not have PrimaryClip")
        }
        return false
    }

    private fun clearClipboard(clipboardManager: ClipboardManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboardManager.clearPrimaryClip()
        } else {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }

    companion object {
        private const val TAG = "ClearClipboardBroadcastReceiver"
        private const val INTENT_FILTER = "ClearClipboardBroadcastReceiver.CLEAR_CLIPBOARD"
        private const val EXPECTED_CONTENTS_KEY = "expected_contents"

        fun prepareIntent(context: Context, expectedClipboardContent: EncryptedString): Intent {
            return Intent(context, ClearClipboardBroadcastReceiver::class.java).apply {
                action = INTENT_FILTER
                putExtra(EXPECTED_CONTENTS_KEY, expectedClipboardContent)
            }
        }
    }

}
