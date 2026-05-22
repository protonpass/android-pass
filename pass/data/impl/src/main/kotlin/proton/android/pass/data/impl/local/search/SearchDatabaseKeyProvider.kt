/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.local.search

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.log.api.PassLogger
import java.io.File
import javax.crypto.KeyGenerator
import javax.inject.Inject
import javax.inject.Singleton

interface SearchDatabaseKeyProvider {
    suspend fun getOrCreateKey(): ByteArray
}

@Singleton
class SearchDatabaseKeyProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionContextProvider: EncryptionContextProvider
) : SearchDatabaseKeyProvider {

    override suspend fun getOrCreateKey(): ByteArray {
        val file = File(context.dataDir, KEY_FILE_NAME)

        return if (file.exists()) {
            runCatching {
                val encrypted = EncryptedByteArray(file.readBytes())
                encryptionContextProvider.withEncryptionContext { decrypt(encrypted) }
            }.fold(
                onSuccess = { it },
                onFailure = {
                    PassLogger.w(TAG, "Failed to decrypt key, generating new one")
                    PassLogger.w(TAG, it)
                    generateAndStoreKey(file)
                }
            )
        } else {
            generateAndStoreKey(file)
        }
    }

    private fun generateAndStoreKey(file: File): ByteArray {
        val databaseKey = generateRandomKey()
        val encrypted = encryptionContextProvider.withEncryptionContext { encrypt(databaseKey) }
        file.writeBytes(encrypted.array)
        return databaseKey
    }

    private fun generateRandomKey(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey().encoded
    }

    companion object {
        private const val TAG = "SearchDatabaseKeyProvider"
        private const val KEY_FILE_NAME = "search_db_key.enc"
        private const val KEY_ALGORITHM = "AES"
        private const val KEY_SIZE = 256
    }
}
