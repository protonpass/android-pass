package me.proton.android.pass.clipboard.api

interface ClipboardManager {
    fun copyToClipboard(text: String, clearAfterSeconds: Long? = 120)
}
