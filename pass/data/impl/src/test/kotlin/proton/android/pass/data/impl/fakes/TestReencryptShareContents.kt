package proton.android.pass.data.impl.fakes

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.data.impl.crypto.ReencryptShareContents

class TestReencryptShareContents : ReencryptShareContents {

    private var reencryptResponse: Result<EncryptedByteArray?> =
        Result.failure(IllegalStateException("reencryptResponse not set"))

    fun setResponse(value: Result<EncryptedByteArray?>) {
        reencryptResponse = value
    }

    override fun invoke(contents: String?, key: EncryptionKey): EncryptedByteArray? =
        reencryptResponse.getOrThrow()
}
