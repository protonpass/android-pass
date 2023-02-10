package proton.android.pass.clipboard.api

import androidx.annotation.WorkerThread

interface ClipboardManager {

    @WorkerThread
    fun copyToClipboard(text: String, clearAfterSeconds: Long? = 120, isSecure: Boolean = false)
}
