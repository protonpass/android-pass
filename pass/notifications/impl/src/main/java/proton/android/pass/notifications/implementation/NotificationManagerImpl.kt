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
import proton.android.pass.domain.PendingInvite
import proton.android.pass.notifications.api.MainActivityAnnotation
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject
import android.app.NotificationManager as AndroidNotificationManager
import me.proton.core.presentation.R as CoreR

class NotificationManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @MainActivityAnnotation private val mainActivityClass: Class<*>,
    private val notificationManagerCompat: NotificationManagerCompat
) : NotificationManager {

    override fun hasNotificationPermission(): Boolean = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
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

        NotificationCompat.Builder(context, AUTOFILL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.autofill_notification_copy_to_clipboard_title))
            .setContentText(context.getString(R.string.autofill_notification_copy_to_clipboard_body))
            .setAutoCancel(true)
            .setTimeoutAfter(NOTIFICATION_TIMEOUT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .also { notificationBuilder ->
                showNotification(COPY_TO_CLIPBOARD_UNIQUE_ID, notificationBuilder)
            }
    }

    override fun sendReceivedInviteNotification(pendingInvite: PendingInvite) {
        createUpdatesNotificationChannel()

        val (contentTitle, contentText, notificationId) = when (pendingInvite) {
            is PendingInvite.Item -> Triple(
                first = context.getString(R.string.new_item_invite_notification_title),
                second = context.getString(
                    R.string.new_item_invite_notification_message,
                    pendingInvite.inviterEmail
                ),
                third = ITEM_INVITE_RECEIVED_UNIQUE_ID
            )

            is PendingInvite.Vault -> Triple(
                first = context.getString(R.string.new_vault_invite_notification_title),
                second = context.getString(
                    R.string.new_vault_invite_notification_message,
                    pendingInvite.inviterEmail
                ),
                third = VAULT_INVITE_RECEIVED_UNIQUE_ID
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, mainActivityClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationCompat.Builder(context, UPDATES_CHANNEL_ID)
            .setSmallIcon(CoreR.drawable.ic_proton_brand_proton_pass)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .also { notificationBuilder ->
                showNotification(notificationId, notificationBuilder)
            }
    }

    override fun removeReceivedInviteNotification(pendingInvite: PendingInvite) {
        when (pendingInvite) {
            is PendingInvite.Item -> ITEM_INVITE_RECEIVED_UNIQUE_ID
            is PendingInvite.Vault -> VAULT_INVITE_RECEIVED_UNIQUE_ID
        }.also(::removeNotification)
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

    private fun showNotification(notificationId: Int, notificationBuilder: NotificationCompat.Builder) {
        if (hasNotificationPermission()) {
            notificationManagerCompat.notify(notificationId, notificationBuilder.build())
        }
    }

    private fun removeNotification(notificationId: Int) {
        notificationManagerCompat.cancel(notificationId)
    }

    private companion object {
        private const val NOTIFICATION_TIMEOUT = 2000L
        private const val COPY_TO_CLIPBOARD_UNIQUE_ID = 1
        private const val ITEM_INVITE_RECEIVED_UNIQUE_ID = 3
        private const val VAULT_INVITE_RECEIVED_UNIQUE_ID = 4
        private const val AUTOFILL_CHANNEL_ID = "AUTOFILL"
        private const val UPDATES_CHANNEL_ID = "UPDATES"
    }

}
