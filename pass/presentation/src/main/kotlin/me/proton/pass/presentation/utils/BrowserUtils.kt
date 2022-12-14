package me.proton.pass.presentation.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import me.proton.android.pass.log.PassLogger
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.android.pass.data.api.UrlSanitizer

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
            .onError {
                val message = "Could not find a suitable url"
                PassLogger.i(TAG, it ?: Exception(message), message)
            }
    }
}
