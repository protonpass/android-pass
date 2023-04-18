package proton.android.pass.crypto.fakes.context

import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestEncryptionContextProvider @Inject constructor() : EncryptionContextProvider {

    private val context: EncryptionContext = TestEncryptionContext

    override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R = block(
        context
    )

    override fun <R> withEncryptionContext(
        key: EncryptionKey,
        block: EncryptionContext.() -> R
    ): R = block(context)

    override suspend fun <R> withEncryptionContextSuspendable(
        block: suspend EncryptionContext.() -> R
    ): R = block(context)

    override suspend fun <R> withEncryptionContextSuspendable(
        key: EncryptionKey,
        block: suspend EncryptionContext.() -> R
    ): R = block(context)
}

