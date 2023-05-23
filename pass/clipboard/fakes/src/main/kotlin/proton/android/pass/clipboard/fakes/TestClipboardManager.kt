package proton.android.pass.clipboard.fakes

import proton.android.pass.clipboard.api.ClipboardManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestClipboardManager @Inject constructor() : ClipboardManager {

    private var contents: String = ""

    fun getContents() = contents

    override fun copyToClipboard(text: String, isSecure: Boolean) {
        contents = text
    }

    override fun getClipboardContent(): Result<String> = Result.success("")
}
