package proton.android.pass.clipboard.api

import androidx.annotation.WorkerThread

interface ClipboardManager {

    fun copyToClipboard(text: String, isSecure: Boolean = false)

    @WorkerThread
    fun getClipboardContent(): Result<String>
}
