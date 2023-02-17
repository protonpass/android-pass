package proton.android.pass.autofill.e2e

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import javax.inject.Inject

object FakeEncryptionContext : EncryptionContext {
    override fun encrypt(content: String): EncryptedString = content

    override fun encrypt(content: ByteArray, tag: EncryptionTag?): EncryptedByteArray =
        EncryptedByteArray(content)

    override fun decrypt(content: EncryptedString): String = content

    override fun decrypt(content: EncryptedByteArray, tag: EncryptionTag?): ByteArray =
        content.array
}

class FakeEncryptionContextProvider @Inject constructor() : EncryptionContextProvider {
    override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R =
        withEncryptionContext(EncryptionKey(byteArrayOf()), block)

    override fun <R> withEncryptionContext(
        key: EncryptionKey,
        block: EncryptionContext.() -> R
    ): R = block(FakeEncryptionContext)

    override suspend fun <R> withEncryptionContextSuspendable(
        block: suspend EncryptionContext.() -> R
    ): R = withEncryptionContextSuspendable(EncryptionKey(byteArrayOf()), block)

    override suspend fun <R> withEncryptionContextSuspendable(
        key: EncryptionKey,
        block: suspend EncryptionContext.() -> R
    ): R = block(FakeEncryptionContext)
}
