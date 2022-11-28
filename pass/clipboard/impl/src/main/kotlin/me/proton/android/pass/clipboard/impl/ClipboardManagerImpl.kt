package me.proton.android.pass.clipboard.impl

import android.content.ClipData
import android.content.Context
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

        val data = if (isSecure) {
            ClipData.newPlainText("·······", text)
        } else {
            ClipData.newPlainText(text, text)
        }
        androidClipboard.setPrimaryClip(data)
        clearAfterSeconds?.let { scheduler.schedule(it, text) }
    }

    companion object {
        private const val TAG = "ClipboardManagerImpl"
    }
}
