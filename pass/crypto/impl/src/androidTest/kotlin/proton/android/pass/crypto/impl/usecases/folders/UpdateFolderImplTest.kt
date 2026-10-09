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

package proton.android.pass.crypto.impl.usecases.folders

import com.google.common.truth.Truth.assertThat
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.crypto.impl.usecases.UnknownProtobufField
import proton.android.pass.commonrust.impl.ItemContentSerializerImpl

class UpdateFolderImplTest {

    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var updateFolder: UpdateFolderImpl
    private val itemContentSerializer = ItemContentSerializerImpl()

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        updateFolder = UpdateFolderImpl(encryptionContextProvider, itemContentSerializer)
    }

    @Test
    fun updateFolderReturnsEncryptedFolderData() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val folderName = "Updated Folder"

        val result = updateFolder.update(folderKey, keyRotation, null, folderName)

        assertThat(result.keyRotation).isEqualTo(keyRotation)
        assertThat(result.contentFormatVersion).isEqualTo(1)
        assertThat(result.content).isNotEmpty()
    }

    @Test
    fun updateFolderEncryptsNewFolderNameCorrectly() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val newFolderName = "Renamed Folder"

        val result = updateFolder.update(folderKey, keyRotation, null, newFolderName)

        // Decrypt the content to verify it contains the new folder name
        val decryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result.content))
            decrypt(encryptedBytes)
        }

        val folder = itemContentSerializer.deserializeFolder(decryptedContent)
        assertThat(folder).isEqualTo(newFolderName)
    }

    @Test
    fun updateFolderPreservesKeyRotation() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 42L
        val folderName = "Folder"

        val result = updateFolder.update(folderKey, keyRotation, null, folderName)

        assertThat(result.keyRotation).isEqualTo(keyRotation)
    }

    @Test
    fun updateFolderUsesExistingFolderKey() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val folderName1 = "First Name"
        val folderName2 = "Second Name"

        val result1 = updateFolder.update(folderKey.clone(), keyRotation, null, folderName1)
        val result2 = updateFolder.update(folderKey.clone(), keyRotation, null, folderName2)

        // Both results should be decryptable with the same folder key
        val decryptedContent1 = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result1.content))
            decrypt(encryptedBytes)
        }

        val decryptedContent2 = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result2.content))
            decrypt(encryptedBytes)
        }

        val folder1 = itemContentSerializer.deserializeFolder(decryptedContent1)
        val folder2 = itemContentSerializer.deserializeFolder(decryptedContent2)

        assertThat(folder1).isEqualTo(folderName1)
        assertThat(folder2).isEqualTo(folderName2)
    }

    @Test
    fun updateFolderHandlesSpecialCharactersInName() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val folderName = "Folder 🚀 with special chars & symbols"

        val result = updateFolder.update(folderKey, keyRotation, null, folderName)

        // Decrypt and verify the name is preserved
        val decryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result.content))
            decrypt(encryptedBytes)
        }

        val folder = itemContentSerializer.deserializeFolder(decryptedContent)
        assertThat(folder).isEqualTo(folderName)
    }

    @Test
    fun updateKeepsUnknownFieldsFromOriginalContent() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val originalContent = itemContentSerializer.serializeFolder("Old Name") + UnknownProtobufField.bytes
        val originalEncryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            Base64.encodeBase64String(encrypt(originalContent, EncryptionTag.FolderContent).array)
        }

        val result = updateFolder.update(folderKey, keyRotation, originalEncryptedContent, "New Name")

        val decryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result.content))
            decrypt(encryptedBytes)
        }

        val folder = itemContentSerializer.deserializeFolder(decryptedContent)
        assertThat(folder).isEqualTo("New Name")
        assertThat(UnknownProtobufField.isContainedIn(decryptedContent)).isTrue()
    }
}
