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
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@AndroidEntryPoint
class ClearClipboardBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var encryptionContextProvider: EncryptionContextProvider

    @Inject
    lateinit var clipboardManager: ClipboardManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action != INTENT_FILTER) return
        PassLogger.d(TAG, "Broadcast receiver invoked")

        val encryptedExpected = intent.getStringExtra(EXPECTED_CONTENTS_KEY)
        if (encryptedExpected == null) {
            PassLogger.w(TAG, "Could not get expected clipboard contents")
            return
        }

        // Check if it has primary clip
        if (shouldClearClipboard(clipboardManager, encryptedExpected)) {
            clipboardManager.clearClipboard()
        }
    }

    private fun shouldClearClipboard(
        clipboardManager: ClipboardManager,
        expected: EncryptedString
    ): Boolean {
        val result = clipboardManager.getClipboardContent()
        val contentsMatch = result.getOrNull() == encryptionContextProvider.withEncryptionContext {
            decrypt(expected)
        }

        if (!contentsMatch) {
            PassLogger.i(TAG, "Did not clear clipboard as it did not have the expected contents")
        }

        return contentsMatch
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
