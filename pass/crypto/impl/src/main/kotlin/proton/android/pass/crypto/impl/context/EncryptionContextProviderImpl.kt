package proton.android.pass.crypto.impl.context

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
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
        val context = EncryptionContextImpl(key)
        val res = block(context)
        key.clear()
        return res
    }

    override suspend fun <R> withEncryptionContextSuspendable(
        block: suspend EncryptionContext.() -> R
    ): R {
        val key = getKey()
        val context = EncryptionContextImpl(key)
        val res = block(context)
        key.clear()
        return res
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
        val encrypted = keyStoreCrypto.encrypt(PlainByteArray(key.key.clone()))
        file.writeBytes(encrypted.array)

        return key
    }

    companion object {
        private const val keyFileName = "pass.key"
    }
}
