/*
 * Copyright (c) 2025 Proton AG
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
import proton_pass_folder_v1.FolderV1

class UpdateFolderImplTest {

    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var updateFolder: UpdateFolderImpl

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        updateFolder = UpdateFolderImpl(encryptionContextProvider)
    }

    @Test
    fun `update folder returns encrypted folder data`() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val folderName = "Updated Folder"

        val result = updateFolder.update(folderKey, keyRotation, folderName)

        assertThat(result.keyRotation).isEqualTo(keyRotation)
        assertThat(result.contentFormatVersion).isEqualTo(1)
        assertThat(result.content).isNotEmpty()
    }

    @Test
    fun `update folder encrypts new folder name correctly`() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val newFolderName = "Renamed Folder"

        val result = updateFolder.update(folderKey, keyRotation, newFolderName)

        // Decrypt the content to verify it contains the new folder name
        val decryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result.content))
            decrypt(encryptedBytes)
        }

        val folder = FolderV1.Folder.parseFrom(decryptedContent)
        assertThat(folder.name).isEqualTo(newFolderName)
    }

    @Test
    fun `update folder preserves key rotation`() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 42L
        val folderName = "Folder"

        val result = updateFolder.update(folderKey, keyRotation, folderName)

        assertThat(result.keyRotation).isEqualTo(keyRotation)
    }

    @Test
    fun `update folder uses existing folder key`() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val folderName1 = "First Name"
        val folderName2 = "Second Name"

        val result1 = updateFolder.update(folderKey.clone(), keyRotation, folderName1)
        val result2 = updateFolder.update(folderKey.clone(), keyRotation, folderName2)

        // Both results should be decryptable with the same folder key
        val decryptedContent1 = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result1.content))
            decrypt(encryptedBytes)
        }

        val decryptedContent2 = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result2.content))
            decrypt(encryptedBytes)
        }

        val folder1 = FolderV1.Folder.parseFrom(decryptedContent1)
        val folder2 = FolderV1.Folder.parseFrom(decryptedContent2)

        assertThat(folder1.name).isEqualTo(folderName1)
        assertThat(folder2.name).isEqualTo(folderName2)
    }

    @Test
    fun `update folder handles special characters in name`() {
        val folderKey = EncryptionKey.generate()
        val keyRotation = 1L
        val folderName = "Folder 🚀 with special chars & symbols"

        val result = updateFolder.update(folderKey, keyRotation, folderName)

        // Decrypt and verify the name is preserved
        val decryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            val encryptedBytes = EncryptedByteArray(Base64.decodeBase64(result.content))
            decrypt(encryptedBytes)
        }

        val folder = FolderV1.Folder.parseFrom(decryptedContent)
        assertThat(folder.name).isEqualTo(folderName)
    }
}
