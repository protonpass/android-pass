package me.proton.android.pass.clipboard.fakes

import me.proton.android.pass.clipboard.api.ClipboardManager

class TestClipboardManager : ClipboardManager {

    private var contents: String = ""
    private var _clearAfterSeconds: Long? = null

    fun getContents() = contents
    fun getClearAfterSeconds() = _clearAfterSeconds

    override fun copyToClipboard(text: String, clearAfterSeconds: Long?, isSecure: Boolean) {
        contents = text
        _clearAfterSeconds = clearAfterSeconds
    }
}
