package proton.android.pass.notifications.fakes

import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject

class TestNotificationManager @Inject constructor() : NotificationManager {
    override fun sendNotification() {
        // no op
    }
}
