/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.crypto.impl.context

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock
import kotlin.experimental.xor

@Singleton
class EncryptionContextProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val appDispatchers: AppDispatchers
) : EncryptionContextProvider {

    private val lock = ReentrantReadWriteLock()
    private var storedKey: ByteArray? = null

    override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R {
        val key = runBlocking { getKey() }
        return withEncryptionContext(key, block)
    }

    @Suppress("TooGenericExceptionCaught", "RethrowCaughtException")
    override fun <R> withEncryptionContext(key: EncryptionKey, block: EncryptionContext.() -> R): R {
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

    override suspend fun <R> withEncryptionContextSuspendable(block: suspend EncryptionContext.() -> R): R {
        val key = getKey()
        return withEncryptionContextSuspendable(key, block)
    }

    @Suppress("TooGenericExceptionCaught", "RethrowCaughtException")
    override suspend fun <R> withEncryptionContextSuspendable(
        key: EncryptionKey,
        block: suspend EncryptionContext.() -> R
    ): R = withContext(appDispatchers.default) {
        val context = EncryptionContextImpl(key)
        try {
            val res = block(context)
            return@withContext res
        } catch (e: Throwable) {
            throw e
        } finally {
            key.clear()
        }
    }

    private suspend fun getKey(): EncryptionKey = withContext(appDispatchers.io) {
        // Try to get it from the stored value
        val readLock = lock.readLock()
        readLock.withLock {
            storedKey?.let { stored ->
                val deobfuscated = deobfuscateKey(stored)
                return@withContext EncryptionKey(deobfuscated)
            }
        }

        // It was not stored. Try to get it from the file or generate it
        val writeLock = lock.writeLock()
        writeLock.withLock {
            // Check again if it's stored to see if other thread already stored it
            storedKey?.let { stored ->
                val deobfuscated = deobfuscateKey(stored)
                return@withLock EncryptionKey(deobfuscated)
            }

            // Guaranteed it's not stored. Read it or generate it
            val file = File(context.dataDir, KEY_FILE_NAME)
            val key = if (file.exists()) {
                val encryptedKey = file.readBytes()
                val decryptedKey = keyStoreCrypto.decrypt(EncryptedByteArray(encryptedKey))
                EncryptionKey(decryptedKey.array)
            } else {
                generateKey(file)
            }

            // Store the key obfuscated in memory. We can do it as we are in the writeLock context
            storedKey = obfuscateKey(key.value())

            // Return the key to be used
            key
        }
    }

    private fun generateKey(file: File): EncryptionKey {
        val key = EncryptionKey.generate()
        val encrypted = keyStoreCrypto.encrypt(PlainByteArray(key.value()))
        file.writeBytes(encrypted.array)

        return key
    }

    companion object {
        private const val KEY_FILE_NAME = "pass.key"
        private const val XOR_KEY = 0xDE.toByte()

        private fun obfuscateKey(input: ByteArray): ByteArray {
            val obfuscated = ByteArray(input.size + 2)
            for (i in input.indices) {
                obfuscated[i] = input[i] xor XOR_KEY
            }
            obfuscated[input.size] = XOR_KEY
            obfuscated[input.size + 1] = XOR_KEY

            return obfuscated
        }

        private fun deobfuscateKey(input: ByteArray): ByteArray {
            if (input[input.size - 1] != XOR_KEY || input[input.size - 2] != XOR_KEY) {
                throw IllegalStateException("Invalid obfuscated key")
            }

            val deobfuscated = ByteArray(input.size - 2)
            for (i in deobfuscated.indices) {
                deobfuscated[i] = input[i] xor XOR_KEY
            }

            return deobfuscated
        }
    }
}
