package me.proton.android.pass.data.fakes.crypto

import me.proton.android.pass.data.api.crypto.EncryptionContext
import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import javax.inject.Inject

class TestEncryptionContextProvider @Inject constructor() : EncryptionContextProvider {

    override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R = block(TestEncryptionContext)

    override suspend fun <R> withEncryptionContextSuspendable(block: suspend EncryptionContext.() -> R): R =
        block(TestEncryptionContext)
}
