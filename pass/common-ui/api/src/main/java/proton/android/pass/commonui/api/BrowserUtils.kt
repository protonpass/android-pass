package proton.android.pass.commonui.api

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.log.api.PassLogger

object BrowserUtils {
    const val TAG = "BrowserUtils"

    fun openWebsite(context: Context, website: String) {
        UrlSanitizer.sanitize(website)
            .onSuccess {
                try {
                    val i = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(it)
                    }
                    context.startActivity(i)
                } catch (e: ActivityNotFoundException) {
                    val message = "Could not find a suitable activity"
                    PassLogger.i(TAG, e, message)
                }
            }
            .onFailure {
                PassLogger.i(TAG, it, "Could not find a suitable url")
            }
    }
}
