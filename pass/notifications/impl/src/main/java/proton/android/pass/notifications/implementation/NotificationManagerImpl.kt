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
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.presentation.R as CoreR
import proton.android.pass.notifications.api.MainActivityAnnotation
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject
import android.app.NotificationManager as AndroidNotificationManager

class NotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @MainActivityAnnotation private val mainActivityClass: Class<*>
) : NotificationManager {

    override fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            val result = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            result == PackageManager.PERMISSION_GRANTED
        }

    override fun sendNotification() {
        createAutofillNotificationChannel()
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

    override fun sendReceivedInviteNotification() {
        createUpdatesNotificationChannel()
        val pendingIntent = PendingIntent.getActivity(
            context,
            INVITE_RECEIVED_UNIQUE_ID,
            Intent(context, mainActivityClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, UPDATES_CHANNEL_ID)
            .setSmallIcon(CoreR.drawable.ic_proton_brand_proton_pass)
            .setContentTitle(context.getString(R.string.updates_new_invite_received_title))
            .setContentText(context.getString(R.string.updates_new_invite_received_message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            notify(INVITE_RECEIVED_UNIQUE_ID, builder.build())
        }
    }

    private fun createAutofillNotificationChannel() {
        createNotificationChannel(
            channelId = AUTOFILL_CHANNEL_ID,
            name = context.getString(R.string.autofill_notification_channel_name)
        )
    }

    private fun createUpdatesNotificationChannel() {
        createNotificationChannel(
            channelId = UPDATES_CHANNEL_ID,
            name = context.getString(R.string.updates_notification_channel_name)
        )
    }

    private fun createNotificationChannel(channelId: String, name: String) {
        val channel = NotificationChannel(
            channelId,
            name,
            AndroidNotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            this.context.getSystemService(AndroidNotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_TIMEOUT = 2000L
        private const val COPY_TO_CLIPBOARD_UNIQUE_ID = 1
        private const val INVITE_RECEIVED_UNIQUE_ID = 3
        private const val AUTOFILL_CHANNEL_ID = "AUTOFILL"
        private const val UPDATES_CHANNEL_ID = "UPDATES"
    }
}
