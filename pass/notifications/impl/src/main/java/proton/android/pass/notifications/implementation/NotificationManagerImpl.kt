package proton.android.pass.notifications.implementation

import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject
import android.app.NotificationManager as AndroidNotificationManager

class NotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
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
        private const val AUTOFILL_CHANNEL_ID = "AUTOFILL"
    }
}
