/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.crypto.impl.usecases.folders

import com.google.common.truth.Truth.assertThat
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider

class MoveFolderImplTest {

    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var moveFolder: MoveFolderImpl

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        moveFolder = MoveFolderImpl(encryptionContextProvider)
    }

    @Test
    fun `reencryptFolderKey returns base64 encoded key`() {
        val folderKey = EncryptionKey.generate()
        val newParentKey = EncryptionKey.generate()

        val result = moveFolder.reencryptFolderKey(folderKey, newParentKey)

        assertThat(result).isNotEmpty()
        // Verify it's valid base64
        val decoded = Base64.decodeBase64(result)
        assertThat(decoded).isNotEmpty()
    }

    @Test
    fun `reencryptFolderKey can be decrypted with new parent key`() {
        val folderKey = EncryptionKey.generate()
        val originalFolderKeyValue = folderKey.value().copyOf()
        val newParentKey = EncryptionKey.generate()

        val reencryptedKey = moveFolder.reencryptFolderKey(folderKey, newParentKey)

        // Decrypt the re-encrypted folder key using the new parent key
        val decryptedFolderKey = encryptionContextProvider.withEncryptionContext(newParentKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(reencryptedKey))
            decrypt(encryptedBytes)
        }

        // The decrypted folder key should match the original folder key value
        assertThat(decryptedFolderKey).isEqualTo(originalFolderKeyValue)
    }

    @Test
    fun `reencryptFolderKey preserves folder key value`() {
        val folderKey = EncryptionKey.generate()
        val originalValue = folderKey.value().copyOf()
        val parentKey1 = EncryptionKey.generate()
        val parentKey2 = EncryptionKey.generate()

        // Encrypt with first parent key
        val encrypted1 = moveFolder.reencryptFolderKey(folderKey.clone(), parentKey1)

        // Decrypt with first parent key
        val decrypted1 = encryptionContextProvider.withEncryptionContext(parentKey1.clone()) {
            val bytes = Base64.decodeBase64(encrypted1)
            decrypt(EncryptedByteArray(bytes))
        }

        // Re-encrypt with second parent key
        val encrypted2 = moveFolder.reencryptFolderKey(EncryptionKey(decrypted1), parentKey2)

        // Decrypt with second parent key
        val decrypted2 = encryptionContextProvider.withEncryptionContext(parentKey2.clone()) {
            val bytes = Base64.decodeBase64(encrypted2)
            decrypt(EncryptedByteArray(bytes))
        }

        // All decrypted values should match the original
        assertThat(decrypted1).isEqualTo(originalValue)
        assertThat(decrypted2).isEqualTo(originalValue)
    }

    @Test
    fun `reencryptFolderKey handles multiple re-encryptions`() {
        val folderKey = EncryptionKey.generate()
        val originalValue = folderKey.value().copyOf()
        val parentKeys = listOf(
            EncryptionKey.generate(),
            EncryptionKey.generate(),
            EncryptionKey.generate()
        )

        var currentEncrypted = ""
        var currentKey = folderKey

        // Re-encrypt with each parent key in sequence
        for (parentKey in parentKeys) {
            currentEncrypted = moveFolder.reencryptFolderKey(currentKey, parentKey)

            // Decrypt to get the folder key for next iteration
            val decrypted = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
                val bytes = Base64.decodeBase64(currentEncrypted)
                decrypt(EncryptedByteArray(bytes))
            }

            currentKey = EncryptionKey(decrypted)
        }

        // Final decrypted value should still match original
        val finalDecrypted = encryptionContextProvider.withEncryptionContext(parentKeys.last().clone()) {
            val bytes = Base64.decodeBase64(currentEncrypted)
            decrypt(EncryptedByteArray(bytes))
        }

        assertThat(finalDecrypted).isEqualTo(originalValue)
    }
}
