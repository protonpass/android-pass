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
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton_pass_folder_v1.FolderV1

class CreateFolderImplTest {

    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var createFolder: CreateFolderImpl

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        createFolder = CreateFolderImpl(encryptionContextProvider)
    }

    @Test
    fun `create folder returns encrypted folder data`() {
        val (shareKey, _) = ShareKeyTestFactory.create()
        val folderName = "My Folder"

        val parentKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val result = createFolder.create(parentKey, shareKey.rotation, folderName)

        assertThat(result.request.keyRotation).isEqualTo(shareKey.rotation)
        assertThat(result.request.contentFormatVersion).isEqualTo(1)
        assertThat(result.request.content).isNotEmpty()
        assertThat(result.request.folderKey).isNotEmpty()
        assertThat(result.folderKey).isNotNull()

        parentKey.clear()
    }

    @Test
    fun `create folder encrypts folder name correctly`() {
        val (shareKey, _) = ShareKeyTestFactory.create()
        val folderName = "Test Folder"

        val parentKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val result = createFolder.create(parentKey, shareKey.rotation, folderName)

        // Decrypt the content to verify it contains the folder name
        val decryptedContent = encryptionContextProvider.withEncryptionContext(result.folderKey.clone()) {
            val encryptedBytes = Base64.decodeBase64(result.request.content)
            decrypt(EncryptedByteArray(encryptedBytes))
        }

        val folder = FolderV1.Folder.parseFrom(decryptedContent)
        assertThat(folder.name).isEqualTo(folderName)

        parentKey.clear()
    }

    @Test
    fun `create folder generates unique folder key`() {
        val (shareKey, _) = ShareKeyTestFactory.create()
        val folderName = "Folder"

        val parentKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val result1 = createFolder.create(parentKey.clone(), shareKey.rotation, folderName)
        val result2 = createFolder.create(parentKey.clone(), shareKey.rotation, folderName)

        // Each call should generate a different folder key
        assertThat(result1.request.folderKey).isNotEqualTo(result2.request.folderKey)

        parentKey.clear()
    }

    @Test
    fun `create folder encrypts folder key with parent key`() {
        val (shareKey, _) = ShareKeyTestFactory.create()
        val folderName = "Encrypted Folder"

        val parentKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val result = createFolder.create(parentKey.clone(), shareKey.rotation, folderName)

        // Verify that the folder key can be decrypted with the parent key
        val decryptedFolderKey = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
            val encryptedFolderKeyBytes = EncryptedByteArray(Base64.decodeBase64(result.request.folderKey))
            decrypt(encryptedFolderKeyBytes)
        }

        assertThat(decryptedFolderKey).isNotEmpty()

        parentKey.clear()
    }

    @Test
    fun `create folder handles special characters in name`() {
        val (shareKey, _) = ShareKeyTestFactory.create()
        val folderName = "Folder with émojis 🎉 and spëcial characters"

        val parentKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val result = createFolder.create(parentKey, shareKey.rotation, folderName)

        // Decrypt and verify the name is preserved
        val decryptedContent = encryptionContextProvider.withEncryptionContext(result.folderKey.clone()) {
            val encryptedBytes = Base64.decodeBase64(result.request.content)
            decrypt(EncryptedByteArray(encryptedBytes))
        }

        val folder = FolderV1.Folder.parseFrom(decryptedContent)
        assertThat(folder.name).isEqualTo(folderName)

        parentKey.clear()
    }
}
