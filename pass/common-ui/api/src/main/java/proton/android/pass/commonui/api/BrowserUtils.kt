package proton.android.pass.commonui.api

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import proton.android.pass.log.api.PassLogger

object BrowserUtils {
    const val TAG = "BrowserUtils"

    fun openWebsite(context: Context, website: String) {
        try {
            val i = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(website)
            }
            context.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            val message = "Could not find a suitable activity"
            PassLogger.i(TAG, e, message)
        }
    }
}
