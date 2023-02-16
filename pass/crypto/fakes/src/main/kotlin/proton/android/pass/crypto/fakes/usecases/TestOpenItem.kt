package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.pass.domain.Share
import proton.pass.domain.key.ShareKey

class TestOpenItem : OpenItem {

    private var output: OpenItemOutput? = null

    fun setOutput(value: OpenItemOutput) {
        output = value
    }

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): OpenItemOutput = output ?: throw IllegalStateException("output not set")
}
