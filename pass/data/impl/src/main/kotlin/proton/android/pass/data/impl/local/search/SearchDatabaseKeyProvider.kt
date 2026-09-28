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
import proton.android.pass.crypto.api.error.LocalEncryptionKeyUnavailableException
import proton.android.pass.log.api.PassLogger
import java.io.File
import javax.crypto.KeyGenerator
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SearchDatabaseKey {
    @JvmInline
    value class Persistent(val passphrase: ByteArray) : SearchDatabaseKey
    data object Unavailable : SearchDatabaseKey
}

interface SearchDatabaseKeyProvider {
    suspend fun getOrCreateKey(): SearchDatabaseKey
}

@Singleton
class SearchDatabaseKeyProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionContextProvider: EncryptionContextProvider
) : SearchDatabaseKeyProvider {

    override suspend fun getOrCreateKey(): SearchDatabaseKey {
        val file = File(context.dataDir, KEY_FILE_NAME)

        return if (file.exists()) {
            readKey(file)
        } else {
            generateAndStoreKey(file)
        }
    }

    private fun readKey(file: File): SearchDatabaseKey = runCatching {
        val encrypted = EncryptedByteArray(file.readBytes())
        encryptionContextProvider.withEncryptionContext { decrypt(encrypted) }
    }.fold(
        onSuccess = { SearchDatabaseKey.Persistent(it) },
        onFailure = { error ->
            if (error is LocalEncryptionKeyUnavailableException) {
                PassLogger.w(TAG, "Local encryption key unavailable, search database key not accessible")
                PassLogger.w(TAG, error)
                SearchDatabaseKey.Unavailable
            } else {
                PassLogger.w(TAG, "Failed to decrypt key, generating new one")
                PassLogger.w(TAG, error)
                generateAndStoreKey(file)
            }
        }
    )

    private fun generateAndStoreKey(file: File): SearchDatabaseKey = runCatching {
        val databaseKey = generateRandomKey()
        val encrypted = encryptionContextProvider.withEncryptionContext { encrypt(databaseKey) }
        deleteStaleDatabase()
        file.writeBytes(encrypted.array)
        databaseKey
    }.fold(
        onSuccess = { SearchDatabaseKey.Persistent(it) },
        onFailure = { error ->
            PassLogger.w(TAG, "Failed to generate search database key")
            PassLogger.w(TAG, error)
            SearchDatabaseKey.Unavailable
        }
    )

    private fun deleteStaleDatabase() {
        val databaseFile = context.getDatabasePath(SearchDatabase.DB_NAME)
        listOf(
            databaseFile,
            File(databaseFile.path + "-wal"),
            File(databaseFile.path + "-shm"),
            File(databaseFile.path + "-journal")
        ).forEach { file ->
            runCatching {
                if (file.exists() && !file.delete()) {
                    PassLogger.w(TAG, "Failed to delete stale search database file: ${file.name}")
                }
            }.onFailure {
                PassLogger.w(TAG, "Error deleting stale search database file: ${file.name}")
                PassLogger.w(TAG, it)
            }
        }
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
