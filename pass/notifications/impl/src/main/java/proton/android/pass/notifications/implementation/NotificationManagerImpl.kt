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

package proton.android.pass.notifications.implementation

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.notifications.api.AutofillDebugActivityAnnotation
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject
import android.app.NotificationManager as AndroidNotificationManager

class NotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @AutofillDebugActivityAnnotation private val autofillDebugActivityClass: Class<*>
) : NotificationManager {

    override fun sendNotification() {
        createAutofillNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, AUTOFILL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.autofill_notification_copy_to_clipboard_title))
            .setContentText(context.getString(R.string.autofill_notification_copy_to_clipboard_body))
            .setAutoCancel(true)
            .setTimeoutAfter(NOTIFICATION_TIMEOUT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            notify(COPY_TO_CLIPBOARD_UNIQUE_ID, builder.build())
        }
    }

    override fun showDebugAutofillNotification() {
        createAutofillNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, AUTOFILL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Autofill debug")
            .setContentText("Autofill debug mode is enabled")
            .setAutoCancel(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, autofillDebugActivityClass).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        with(NotificationManagerCompat.from(context)) {
            notify(AUTOFILL_DEBUG_MODE_UNIQUE_ID, builder.build())
        }
    }

    override fun hideDebugAutofillNotification() {
        with(NotificationManagerCompat.from(context)) {
            cancel(AUTOFILL_DEBUG_MODE_UNIQUE_ID)
        }
    }

    private fun createAutofillNotificationChannel(context: Context) {
        val importance = AndroidNotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            AUTOFILL_CHANNEL_ID,
            context.getString(R.string.autofill_notification_channel_name),
            importance
        )
        val notificationManager =
            this.context.getSystemService(AndroidNotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_TIMEOUT = 2000L
        private const val COPY_TO_CLIPBOARD_UNIQUE_ID = 1
        private const val AUTOFILL_DEBUG_MODE_UNIQUE_ID = 2
        private const val AUTOFILL_CHANNEL_ID = "AUTOFILL"
    }
}
