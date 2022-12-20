package me.proton.android.pass.data.impl.crypto.context

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.proton.android.pass.data.api.crypto.EncryptionContext
import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import me.proton.android.pass.data.api.crypto.EncryptionKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

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
        val bytes = Random.nextBytes(keySize)
        val encrypted = keyStoreCrypto.encrypt(PlainByteArray(bytes.clone()))
        file.writeBytes(encrypted.array)

        return EncryptionKey(bytes)
    }

    companion object {
        private const val keySize = 32
        private const val keyFileName = "pass.key"
    }
}
