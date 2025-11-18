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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ClearClipboardScheduler @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val encryptionContextProvider: EncryptionContextProvider
) {

    fun schedule(delaySeconds: Long, expectedClipboardContents: String) {
        val intent = ClearClipboardBroadcastReceiver.prepareIntent(
            applicationContext,
            encryptionContextProvider.withEncryptionContext { encrypt(expectedClipboardContents) }
        )

        val asPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val delayMilliseconds = delaySeconds.seconds.inWholeMilliseconds
        val timeToTriggerInMillis = System.currentTimeMillis() + delayMilliseconds
        val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            timeToTriggerInMillis,
            asPendingIntent
        )

        PassLogger.i(TAG, "Scheduled ClearClipboardBroadcastReceiver on $delaySeconds seconds")
    }

    companion object {
        private const val TAG = "ClearClipboardScheduler"
    }
}
