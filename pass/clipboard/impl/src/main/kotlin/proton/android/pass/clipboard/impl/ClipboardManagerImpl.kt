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
import android.content.ClipDescription
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.clipboard.api.CouldNotAccessClipboard
import proton.android.pass.clipboard.api.CouldNotGetClipboardContent
import proton.android.pass.clipboard.api.EmptyClipboardContent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.ClearClipboardPreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import android.content.ClipboardManager as AndroidClipboardManager

class ClipboardManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    preferencesRepository: UserPreferencesRepository,
    private val scheduler: ClearClipboardScheduler
) : ClipboardManager {

    private val clearClipboardPreferenceFlow = preferencesRepository.getClearClipboardPreference()

    private val androidClipboard by lazy { context.getSystemService(AndroidClipboardManager::class.java) }

    override fun copyToClipboard(text: String, isSecure: Boolean) {
        if (androidClipboard == null) {
            PassLogger.i(TAG, "Could not get ClipboardManager")
            return
        }

        val clipData = ClipData.newPlainText("pass-contents", text)
        if (isSecure) {
            applySecureFlag(clipData)
        }
        runBlocking(Dispatchers.IO) {
            androidClipboard.setPrimaryClip(clipData)
        }
        when (runBlocking { clearClipboardPreferenceFlow.first() }) {
            ClearClipboardPreference.Never -> {}
            ClearClipboardPreference.S60 -> scheduler.schedule(1.minutes.inWholeSeconds, text)
            ClearClipboardPreference.S180 -> scheduler.schedule(3.minutes.inWholeSeconds, text)
        }
    }

    override fun clearClipboard() {
        if (androidClipboard == null) {
            PassLogger.i(TAG, "Could not get ClipboardManager")
            return
        }

        runCatching {
            runBlocking(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    androidClipboard.clearPrimaryClip()
                } else {
                    androidClipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            }
        }.onSuccess {
            PassLogger.i(TAG, "Successfully cleared clipboard")
        }.onFailure {
            PassLogger.w(TAG, "Could not clear clipboard")
            PassLogger.w(TAG, it)
        }
    }

    override fun getClipboardContent(): Result<String> = runCatching {
        if (androidClipboard == null) {
            PassLogger.i(TAG, "Could not get ClipboardManager")
            throw CouldNotAccessClipboard()
        }
        if (!androidClipboard.hasPrimaryClip() ||
            androidClipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) != true
        ) {
            PassLogger.i(TAG, "Could not get clipboard content")
            throw CouldNotGetClipboardContent()
        }

        val value = androidClipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
        return value?.let { Result.success(it) } ?: throw EmptyClipboardContent()
    }

    private fun applySecureFlag(clipData: ClipData) {
        val key = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ClipDescription.EXTRA_IS_SENSITIVE
        } else {
            "android.content.extra.IS_SENSITIVE"
        }

        clipData.description.extras = PersistableBundle().apply {
            putBoolean(key, true)
        }
    }

    companion object {
        private const val TAG = "ClipboardManagerImpl"
    }
}
