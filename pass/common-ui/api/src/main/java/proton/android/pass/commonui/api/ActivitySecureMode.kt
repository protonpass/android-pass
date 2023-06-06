package proton.android.pass.commonui.api

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.preferences.value

fun Activity.setSecureMode(preference: AllowScreenshotsPreference) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        setRecentsScreenshotEnabled(false)
    }

    if (!preference.value()) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}
