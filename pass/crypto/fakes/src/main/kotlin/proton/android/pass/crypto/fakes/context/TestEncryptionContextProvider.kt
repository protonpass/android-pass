package proton.android.pass.crypto.fakes.context

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import javax.inject.Inject

class TestEncryptionContextProvider @Inject constructor() : EncryptionContextProvider {

    override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R = block(
        TestEncryptionContext
    )

    override suspend fun <R> withEncryptionContextSuspendable(block: suspend EncryptionContext.() -> R): R =
        block(TestEncryptionContext)
}

