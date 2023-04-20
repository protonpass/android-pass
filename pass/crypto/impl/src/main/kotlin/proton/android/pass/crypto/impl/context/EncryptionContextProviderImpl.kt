package proton.android.pass.crypto.impl.context

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import java.io.File
import javax.inject.Inject

class EncryptionContextProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyStoreCrypto: KeyStoreCrypto
) : EncryptionContextProvider {

    override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R {
        val key = runBlocking { getKey() }
        return withEncryptionContext(key, block)
    }

    @Suppress("TooGenericExceptionCaught", "RethrowCaughtException")
    override fun <R> withEncryptionContext(
        key: EncryptionKey,
        block: EncryptionContext.() -> R
    ): R {
        val context = EncryptionContextImpl(key)
        try {
            val res = block(context)
            return res
        } catch (e: Throwable) {
            throw e
        } finally {
            key.clear()
        }
    }

    override suspend fun <R> withEncryptionContextSuspendable(
        block: suspend EncryptionContext.() -> R
    ): R {
        val key = getKey()
        return withEncryptionContextSuspendable(key, block)
    }

    @Suppress("TooGenericExceptionCaught", "RethrowCaughtException")
    override suspend fun <R> withEncryptionContextSuspendable(
        key: EncryptionKey,
        block: suspend EncryptionContext.() -> R
    ): R {
        val context = EncryptionContextImpl(key)
        try {
            val res = block(context)
            return res
        } catch (e: Throwable) {
            throw e
        } finally {
            key.clear()
        }
    }

    private suspend fun getKey(): EncryptionKey = withContext(Dispatchers.IO) {
        val file = File(context.dataDir, keyFileName)
        if (file.exists()) {
            val encryptedKey = file.readBytes()
            val decryptedKey = keyStoreCrypto.decrypt(EncryptedByteArray(encryptedKey))
            EncryptionKey(decryptedKey.array)
        } else {
            generateKey(file)
        }
    }

    private fun generateKey(file: File): EncryptionKey {
        val key = EncryptionKey.generate()
        val encrypted = keyStoreCrypto.encrypt(PlainByteArray(key.value()))
        file.writeBytes(encrypted.array)

        return key
    }

    companion object {
        private const val keyFileName = "pass.key"
    }
}
