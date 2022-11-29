package me.proton.android.pass.clipboard.impl

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.android.pass.clipboard.api.ClipboardManager
import me.proton.android.pass.log.PassLogger
import javax.inject.Inject
import android.content.ClipboardManager as AndroidClipboardManager

class ClipboardManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduler: ClearClipboardScheduler
) : ClipboardManager {
    override fun copyToClipboard(text: String, clearAfterSeconds: Long?, isSecure: Boolean) {
        val androidClipboard = context.getSystemService(AndroidClipboardManager::class.java)
        if (androidClipboard == null) {
            PassLogger.i(TAG, "Could not get ClipboardManager")
            return
        }

        val clipData = ClipData.newPlainText("pass-contents", text)
        if (isSecure) {
            applySecureFlag(clipData)
        }
        androidClipboard.setPrimaryClip(clipData)
        clearAfterSeconds?.let { scheduler.schedule(it, text) }
    }

    private fun applySecureFlag(clipData: ClipData) {
        val key = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ClipDescription.EXTRA_IS_SENSITIVE
        } else {
            "android.content.extra.IS_SENSITIVE"
        }

        clipData.description.extras = PersistableBundle().apply {
            putBoolean(key, true)
        }
    }

    companion object {
        private const val TAG = "ClipboardManagerImpl"
    }
}
