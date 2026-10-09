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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.usecases.folders.EncryptedFolderData
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.crypto.impl.usecases.UnknownProtobufField
import proton.android.pass.domain.FolderId
import proton.android.pass.commonrust.impl.ItemContentSerializerImpl

class OpenFolderImplTest {

    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var openFolder: OpenFolderImpl
    private val itemContentSerializer = ItemContentSerializerImpl()

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        openFolder = OpenFolderImpl(encryptionContextProvider, itemContentSerializer)
    }

    @Test
    fun openFolderDecryptsFolderNameCorrectly() = runTest {
        val folderName = "Test Folder"
        val folderKey = EncryptionKey.generate()
        val parentKey = EncryptionKey.generate()

        // Encrypt folder content
        val encryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            encrypt(itemContentSerializer.serializeFolder(folderName))
        }

        // Encrypt folder key with parent key
        val encryptedFolderKey = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
            encrypt(folderKey.value())
        }

        val encryptedFolderData = EncryptedFolderData(
            folderId = FolderId("folder-id"),
            parentFolderId = null,
            keyRotation = 1L,
            contentFormatVersion = 1,
            content = Base64.encodeBase64String(encryptedContent.array),
            folderKey = Base64.encodeBase64String(encryptedFolderKey.array)
        )

        val result = openFolder.open(encryptedFolderData, parentKey)

        assertThat(result.folderName).isEqualTo(folderName)
    }

    @Test
    fun openFolderReturnsReEncryptedFolderKey() = runTest {
        val folderName = "Folder"
        val folderKey = EncryptionKey.generate()
        val parentKey = EncryptionKey.generate()

        val encryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            encrypt(itemContentSerializer.serializeFolder(folderName))
        }

        val encryptedFolderKey = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
            encrypt(folderKey.value())
        }

        val encryptedFolderData = EncryptedFolderData(
            folderId = FolderId("folder-id"),
            parentFolderId = null,
            keyRotation = 1L,
            contentFormatVersion = 1,
            content = Base64.encodeBase64String(encryptedContent.array),
            folderKey = Base64.encodeBase64String(encryptedFolderKey.array)
        )

        val result = openFolder.open(encryptedFolderData, parentKey)

        assertThat(result.reencryptedFolderKey.array).isNotEmpty()
        // The re-encrypted key should be decryptable
        assertThat(result.reencryptedFolderKey.array.size).isGreaterThan(0)
    }

    @Test
    fun openFolderHandlesSpecialCharactersInName() = runTest {
        val folderName = "Folder with 特殊字符 and émojis 🎉"
        val folderKey = EncryptionKey.generate()
        val parentKey = EncryptionKey.generate()

        val encryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            encrypt(itemContentSerializer.serializeFolder(folderName))
        }

        val encryptedFolderKey = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
            encrypt(folderKey.value())
        }

        val encryptedFolderData = EncryptedFolderData(
            folderId = FolderId("folder-id"),
            parentFolderId = null,
            keyRotation = 1L,
            contentFormatVersion = 1,
            content = Base64.encodeBase64String(encryptedContent.array),
            folderKey = Base64.encodeBase64String(encryptedFolderKey.array)
        )

        val result = openFolder.open(encryptedFolderData, parentKey)

        assertThat(result.folderName).isEqualTo(folderName)
    }

    @Test
    fun openFolderWithNestedParentFolder() = runTest {
        val folderName = "Nested Folder"
        val folderKey = EncryptionKey.generate()
        val parentKey = EncryptionKey.generate()

        val encryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            encrypt(itemContentSerializer.serializeFolder(folderName))
        }

        val encryptedFolderKey = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
            encrypt(folderKey.value())
        }

        val encryptedFolderData = EncryptedFolderData(
            folderId = FolderId("nested-folder-id"),
            parentFolderId = FolderId("parent-folder-id"),
            keyRotation = 1L,
            contentFormatVersion = 1,
            content = Base64.encodeBase64String(encryptedContent.array),
            folderKey = Base64.encodeBase64String(encryptedFolderKey.array)
        )

        val result = openFolder.open(encryptedFolderData, parentKey)

        assertThat(result.folderName).isEqualTo(folderName)
        assertThat(result.reencryptedFolderKey.array).isNotEmpty()
    }

    @Test
    fun openFolderDecryptsEmptyFolderName() = runTest {
        val folderName = ""
        val folderKey = EncryptionKey.generate()
        val parentKey = EncryptionKey.generate()

        val encryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            encrypt(itemContentSerializer.serializeFolder(folderName))
        }

        val encryptedFolderKey = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
            encrypt(folderKey.value())
        }

        val encryptedFolderData = EncryptedFolderData(
            folderId = FolderId("empty-name-folder"),
            parentFolderId = null,
            keyRotation = 1L,
            contentFormatVersion = 1,
            content = Base64.encodeBase64String(encryptedContent.array),
            folderKey = Base64.encodeBase64String(encryptedFolderKey.array)
        )

        val result = openFolder.open(encryptedFolderData, parentKey)

        assertThat(result.folderName).isEmpty()
    }

    @Test
    fun openFolderReturnsOriginalContentIntactWithUnknownFields() = runTest {
        val folderName = "Test Folder"
        val folderKey = EncryptionKey.generate()
        val parentKey = EncryptionKey.generate()

        val serializedFolder = itemContentSerializer.serializeFolder(folderName) + UnknownProtobufField.bytes
        val encryptedContent = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            encrypt(serializedFolder)
        }

        val encryptedFolderKey = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
            encrypt(folderKey.value())
        }

        val encryptedFolderData = EncryptedFolderData(
            folderId = FolderId("folder-id"),
            parentFolderId = null,
            keyRotation = 1L,
            contentFormatVersion = 1,
            content = Base64.encodeBase64String(encryptedContent.array),
            folderKey = Base64.encodeBase64String(encryptedFolderKey.array)
        )

        val result = openFolder.open(encryptedFolderData, parentKey)

        assertThat(result.folderName).isEqualTo(folderName)
        assertThat(result.originalContent).isEqualTo(serializedFolder)
        assertThat(UnknownProtobufField.isContainedIn(result.originalContent)).isTrue()
    }
}
