package proton.android.pass.crypto.impl.context

import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider

class TestEncryptionContextProvider(private val key: EncryptionKey) : EncryptionContextProvider {
    override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R =
        withEncryptionContext(key, block)

    override fun <R> withEncryptionContext(
        key: EncryptionKey,
        block: EncryptionContext.() -> R
    ): R = block(EncryptionContextImpl(key))

    override suspend fun <R> withEncryptionContextSuspendable(block: suspend EncryptionContext.() -> R): R =
        withEncryptionContextSuspendable(key, block)

    override suspend fun <R> withEncryptionContextSuspendable(
        key: EncryptionKey,
        block: suspend EncryptionContext.() -> R
    ): R = block(EncryptionContextImpl(key))
}
