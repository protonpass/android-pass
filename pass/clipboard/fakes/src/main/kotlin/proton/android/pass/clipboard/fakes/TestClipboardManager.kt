package proton.android.pass.clipboard.fakes

import proton.android.pass.clipboard.api.ClipboardManager
import javax.inject.Inject

class TestClipboardManager @Inject constructor() : ClipboardManager {

    private var contents: String = ""
    private var _clearAfterSeconds: Long? = null

    fun getContents() = contents
    fun getClearAfterSeconds() = _clearAfterSeconds

    override fun copyToClipboard(text: String, clearAfterSeconds: Long?, isSecure: Boolean) {
        contents = text
        _clearAfterSeconds = clearAfterSeconds
    }

    override fun getClipboardContent(): Result<String> = Result.success("")
}
