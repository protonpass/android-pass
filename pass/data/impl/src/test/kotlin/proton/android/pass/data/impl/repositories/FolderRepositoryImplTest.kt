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

package proton.android.pass.data.impl.repositories

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers
import kotlin.test.assertFailsWith
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.FakeOpenFolder
import proton.android.pass.data.impl.fakes.FakeLocalFolderDataSource
import proton.android.pass.data.impl.fakes.FakeLocalFolderKeyDataSource
import proton.android.pass.data.impl.fakes.FakeLocalShareKeyDataSource
import proton.android.pass.data.impl.fakes.FakePassDatabase
import proton.android.pass.data.impl.fakes.FakeRemoteFolderDataSource
import proton.android.pass.data.impl.fakes.FakeShareKeyRepository
import proton.android.pass.data.impl.fakes.mother.FolderApiModelTestFactory
import proton.android.pass.data.impl.fakes.mother.FolderEntityTestFactory
import proton.android.pass.data.impl.fakes.mother.FolderKeyEntityTestFactory
import proton.android.pass.data.impl.remote.RemoteFolderDataSource
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.test.domain.ShareKeyTestFactory
import proton_pass_folder_v1.FolderV1

internal class FolderRepositoryImplTest {

    private val userId = UserId("user-id")
    private val shareId = ShareId("share-id")

    private lateinit var localFolderDataSource: FakeLocalFolderDataSource
    private lateinit var localFolderKeyDataSource: FakeLocalFolderKeyDataSource
    private lateinit var localShareKeyDataSource: FakeLocalShareKeyDataSource
    private lateinit var remoteFolderDataSource: FakeRemoteFolderDataSource
    private lateinit var openFolder: FakeOpenFolder
    private lateinit var shareKeyRepository: FakeShareKeyRepository
    private lateinit var repository: FolderRepositoryImpl

    @Before
    fun setup() {
        localFolderDataSource = FakeLocalFolderDataSource()
        localFolderKeyDataSource = FakeLocalFolderKeyDataSource()
        localShareKeyDataSource = FakeLocalShareKeyDataSource()
        remoteFolderDataSource = FakeRemoteFolderDataSource()
        openFolder = FakeOpenFolder()
        shareKeyRepository = FakeShareKeyRepository()

        repository = FolderRepositoryImpl(
            appDispatchers = FakeAppDispatchers(),
            localFolderDataSource = localFolderDataSource,
            localFolderKeyDataSource = localFolderKeyDataSource,
            localShareKeyDataSource = localShareKeyDataSource,
            remoteFolderDataSource = remoteFolderDataSource,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            openFolder = openFolder,
            shareKeyRepository = shareKeyRepository,
            database = FakePassDatabase()
        )
    }

    @Test
    fun `observeFolders decrypts encrypted content into domain names`() = runTest {
        localFolderDataSource.upsertFolder(
            folderEntity(
                folderId = "root-1",
                name = "Personal",
                parentFolderId = null
            )
        )
        localFolderDataSource.upsertFolder(
            folderEntity(
                folderId = "child-1",
                name = "Nested",
                parentFolderId = "root-1"
            )
        )

        val result = repository.observeFolders(userId, shareId).first()

        assertThat(result).hasSize(1)
        assertThat(result.first().folderId.id).isEqualTo("root-1")
        assertThat(result.first().name).isEqualTo("Personal")
    }

    @Test
    fun `observeFolder returns decrypted folder when present`() = runTest {
        val entity = folderEntity(
            folderId = "folder-1",
            name = "Finance",
            parentFolderId = null
        )
        localFolderDataSource.upsertFolder(entity)

        val result = repository.observeFolder(userId, shareId, FolderId("folder-1")).first()

        assertThat(result?.name).isEqualTo("Finance")
        assertThat(result?.folderId?.id).isEqualTo("folder-1")
    }

    @Test
    fun `createFolder uses local share key for root folder decryption`() = runTest {
        localShareKeyDataSource.setShareKey(
            userId = userId,
            shareId = shareId,
            rotation = 3,
            keyBytes = byteArrayOf(10)
        )
        remoteFolderDataSource.createFolderResponse = FolderApiModelTestFactory.create(
            folderId = "created-root",
            parentFolderId = null,
            keyRotation = 3,
            folderKey = "remote-folder-key"
        )
        openFolder.setOutput(
            folderId = "created-root",
            folderName = "Root Name",
            reencryptedFolderKey = encryptedBytes(99)
        )

        val result = repository.createFolder(
            userId = userId,
            shareId = shareId,
            parentFolderId = null,
            keyRotation = 3,
            contentFormatVersion = 1,
            content = "payload",
            folderKey = "folder-key"
        )

        assertThat(openFolder.calls).hasSize(1)
        assertThat(openFolder.calls.single().parentKey.toList()).isEqualTo(listOf(10.toByte()))
        assertThat(result.name).isEqualTo("Root Name")
        assertThat(localFolderDataSource.memory.single().id).isEqualTo("created-root")
        assertThat(localFolderKeyDataSource.memory.single().encryptedKey.array.toList())
            .isEqualTo(encryptedBytes(99).array.toList())

        val request = remoteFolderDataSource.createFolderCalls.single().request
        assertThat(request.parentFolderId).isNull()
        assertThat(request.keyRotation).isEqualTo(3)
        assertThat(request.content).isEqualTo("payload")
        assertThat(request.folderKey).isEqualTo("folder-key")
    }

    @Test
    fun `createFolder clears transient open folder key after use`() = runTest {
        localShareKeyDataSource.setShareKey(
            userId = userId,
            shareId = shareId,
            rotation = 3,
            keyBytes = byteArrayOf(10)
        )
        remoteFolderDataSource.createFolderResponse = FolderApiModelTestFactory.create(
            folderId = "created-root",
            parentFolderId = null,
            keyRotation = 3,
            folderKey = "remote-folder-key"
        )
        openFolder.setOutput(
            folderId = "created-root",
            folderName = "Root Name",
            reencryptedFolderKey = encryptedBytes(99)
        )

        repository.createFolder(
            userId = userId,
            shareId = shareId,
            parentFolderId = null,
            keyRotation = 3,
            contentFormatVersion = 1,
            content = "payload",
            folderKey = "folder-key"
        )

        assertThat(openFolder.keyReferences).hasSize(1)
        assertFailsWith<IllegalStateException> {
            openFolder.keyReferences.single().value()
        }
    }

    @Test
    fun `createFolder uses parent folder key for nested folder decryption`() = runTest {
        localFolderKeyDataSource.upsertKey(
            FolderKeyEntityTestFactory.create(
                folderId = "parent-folder",
                userId = userId.id,
                shareId = shareId.id,
                keyRotation = 1,
                encryptedKey = encryptedBytes(44)
            )
        )
        remoteFolderDataSource.createFolderResponse = FolderApiModelTestFactory.create(
            folderId = "created-child",
            parentFolderId = "parent-folder",
            keyRotation = 7,
            folderKey = "nested-key"
        )
        openFolder.setOutput(
            folderId = "created-child",
            folderName = "Nested Name",
            reencryptedFolderKey = encryptedBytes(12)
        )

        repository.createFolder(
            userId = userId,
            shareId = shareId,
            parentFolderId = FolderId("parent-folder"),
            keyRotation = 7,
            contentFormatVersion = 1,
            content = "payload",
            folderKey = "child-folder-key"
        )

        assertThat(openFolder.calls.single().parentKey.toList()).isEqualTo(listOf(44.toByte()))
    }

    @Test
    fun `createFolder falls back to share key repository when local share key missing`() = runTest {
        shareKeyRepository.emitGetLatestKeyForShare(
            ShareKeyTestFactory.create(rotation = 8, keyBytes = byteArrayOf(77))
        )
        remoteFolderDataSource.createFolderResponse = FolderApiModelTestFactory.create(
            folderId = "created-root",
            parentFolderId = null,
            keyRotation = 8,
            folderKey = "root-key"
        )
        openFolder.setOutput(
            folderId = "created-root",
            folderName = "Root",
            reencryptedFolderKey = encryptedBytes(5)
        )

        repository.createFolder(
            userId = userId,
            shareId = shareId,
            parentFolderId = null,
            keyRotation = 8,
            contentFormatVersion = 1,
            content = "content",
            folderKey = "folder-key"
        )

        assertThat(openFolder.calls.single().parentKey.toList()).isEqualTo(listOf(77.toByte()))
    }

    @Test
    fun `refreshFolders decrypts root and child using resolved keys`() = runTest {
        localShareKeyDataSource.setShareKey(
            userId = userId,
            shareId = shareId,
            rotation = 2,
            keyBytes = byteArrayOf(21)
        )
        val encryptedChildKey = Base64.encodeBase64String(encryptedBytes(55).array)
        remoteFolderDataSource.retrieveFoldersResponses.add(
            RemoteFolderDataSource.FoldersPage(
                folders = listOf(
                    FolderApiModelTestFactory.create(
                        folderId = "root",
                        parentFolderId = null,
                        keyRotation = 2,
                        folderKey = encryptedChildKey
                    ),
                    FolderApiModelTestFactory.create(
                        folderId = "child",
                        parentFolderId = "root",
                        keyRotation = 2,
                        folderKey = "unused"
                    )
                ),
                lastToken = null
            )
        )
        openFolder.setOutput("root", "Root Folder", encryptedBytes(31))
        openFolder.setOutput("child", "Child Folder", encryptedBytes(32))

        repository.refreshFolders(userId, shareId)

        assertThat(openFolder.calls).hasSize(2)
        assertThat(openFolder.calls[0].folderId).isEqualTo("root")
        assertThat(openFolder.calls[0].parentKey.toList()).isEqualTo(listOf(21.toByte()))
        assertThat(openFolder.calls[1].folderId).isEqualTo("child")
        assertThat(openFolder.calls[1].parentKey.toList()).isEqualTo(listOf(55.toByte()))
        assertThat(localFolderDataSource.memory.map { decryptFolderName(it.encryptedContent) })
            .containsExactly("Root Folder", "Child Folder")
    }

    @Test
    fun `refreshFolders clears transient open folder keys after use`() = runTest {
        localShareKeyDataSource.setShareKey(
            userId = userId,
            shareId = shareId,
            rotation = 2,
            keyBytes = byteArrayOf(21)
        )
        val encryptedChildKey = Base64.encodeBase64String(encryptedBytes(55).array)
        remoteFolderDataSource.retrieveFoldersResponses.add(
            RemoteFolderDataSource.FoldersPage(
                folders = listOf(
                    FolderApiModelTestFactory.create(
                        folderId = "root",
                        parentFolderId = null,
                        keyRotation = 2,
                        folderKey = encryptedChildKey
                    ),
                    FolderApiModelTestFactory.create(
                        folderId = "child",
                        parentFolderId = "root",
                        keyRotation = 2,
                        folderKey = "unused"
                    )
                ),
                lastToken = null
            )
        )
        openFolder.setOutput("root", "Root Folder", encryptedBytes(31))
        openFolder.setOutput("child", "Child Folder", encryptedBytes(32))

        repository.refreshFolders(userId, shareId)

        assertThat(openFolder.keyReferences).hasSize(2)
        openFolder.keyReferences.forEach { key ->
            assertFailsWith<IllegalStateException> {
                key.value()
            }
        }
    }

    @Test
    fun `refreshFolders decrypts orphaned child using local parent key`() = runTest {
        localFolderKeyDataSource.upsertKey(
            FolderKeyEntityTestFactory.create(
                folderId = "missing-parent",
                userId = userId.id,
                shareId = shareId.id,
                keyRotation = 1,
                encryptedKey = encryptedBytes(90)
            )
        )
        remoteFolderDataSource.retrieveFoldersResponses.add(
            RemoteFolderDataSource.FoldersPage(
                folders = listOf(
                    FolderApiModelTestFactory.create(
                        folderId = "orphan",
                        parentFolderId = "missing-parent",
                        keyRotation = 1,
                        folderKey = "orphan-key"
                    )
                ),
                lastToken = null
            )
        )
        openFolder.setOutput("orphan", "Recovered Child", encryptedBytes(8))

        repository.refreshFolders(userId, shareId)

        assertThat(openFolder.calls).hasSize(1)
        assertThat(openFolder.calls.single().parentKey.toList()).isEqualTo(listOf(90.toByte()))
        assertThat(localFolderDataSource.memory.single().id).isEqualTo("orphan")
    }

    @Test
    fun `refreshFolders deduplicates duplicate folder ids before decryption`() = runTest {
        localShareKeyDataSource.setShareKey(
            userId = userId,
            shareId = shareId,
            rotation = 2,
            keyBytes = byteArrayOf(21)
        )
        remoteFolderDataSource.retrieveFoldersResponses.add(
            RemoteFolderDataSource.FoldersPage(
                folders = listOf(
                    FolderApiModelTestFactory.create(folderId = "duplicate", parentFolderId = null, keyRotation = 2),
                    FolderApiModelTestFactory.create(folderId = "duplicate", parentFolderId = null, keyRotation = 2),
                    FolderApiModelTestFactory.create(folderId = "other", parentFolderId = null, keyRotation = 2)
                ),
                lastToken = null
            )
        )
        openFolder.setOutput("duplicate", "Duplicate", encryptedBytes(31))
        openFolder.setOutput("other", "Other", encryptedBytes(32))

        repository.refreshFolders(userId, shareId)

        assertThat(openFolder.calls.map { it.folderId }).containsExactly("duplicate", "other")
        assertThat(localFolderDataSource.memory.map { it.id }).containsExactly("duplicate", "other")
    }

    @Test
    fun `refreshFolders continues when one folder decryption fails`() = runTest {
        localShareKeyDataSource.setShareKey(
            userId = userId,
            shareId = shareId,
            rotation = 2,
            keyBytes = byteArrayOf(21)
        )
        remoteFolderDataSource.retrieveFoldersResponses.add(
            RemoteFolderDataSource.FoldersPage(
                folders = listOf(
                    FolderApiModelTestFactory.create(folderId = "will-fail", parentFolderId = null, keyRotation = 2),
                    FolderApiModelTestFactory.create(folderId = "will-succeed", parentFolderId = null, keyRotation = 2)
                ),
                lastToken = null
            )
        )
        openFolder.setOutput("will-succeed", "Recovered", encryptedBytes(32))

        repository.refreshFolders(userId, shareId)

        assertThat(openFolder.calls.map { it.folderId }).containsExactly("will-fail", "will-succeed")
        assertThat(localFolderDataSource.memory.map { it.id }).containsExactly("will-succeed")
    }

    @Test
    fun `refreshFolders stops traversal when decryption circuit breaker is hit`() = runTest {
        localShareKeyDataSource.setShareKey(
            userId = userId,
            shareId = shareId,
            rotation = 2,
            keyBytes = byteArrayOf(21)
        )

        val folderCount = 1_100
        val folders = (1..folderCount).map { index ->
            FolderApiModelTestFactory.create(
                folderId = "folder-$index",
                parentFolderId = null,
                keyRotation = 2
            )
        }
        remoteFolderDataSource.retrieveFoldersResponses.add(
            RemoteFolderDataSource.FoldersPage(
                folders = folders,
                lastToken = null
            )
        )
        folders.forEachIndexed { index, folder ->
            openFolder.setOutput(folder.folderId, "Folder $index", encryptedBytes(60))
        }

        repository.refreshFolders(userId, shareId)

        assertThat(openFolder.calls.size).isLessThan(folderCount)
        assertThat(localFolderDataSource.memory).hasSize(openFolder.calls.size)
    }

    @Test
    fun `updateFolder maps request and decrypts response`() = runTest {
        localShareKeyDataSource.setShareKey(userId, shareId, rotation = 5, keyBytes = byteArrayOf(61))
        remoteFolderDataSource.updateFolderResponse = FolderApiModelTestFactory.create(
            folderId = "folder-to-update",
            parentFolderId = null,
            keyRotation = 5,
            folderKey = "update-key"
        )
        openFolder.setOutput("folder-to-update", "Updated Name", encryptedBytes(3))

        val result = repository.updateFolder(
            userId = userId,
            shareId = shareId,
            folderId = FolderId("folder-to-update"),
            keyRotation = 5,
            contentFormatVersion = 2,
            content = "updated-content"
        )

        assertThat(result.name).isEqualTo("Updated Name")
        val request = remoteFolderDataSource.updateFolderCalls.single().request
        assertThat(request.keyRotation).isEqualTo(5)
        assertThat(request.contentFormatVersion).isEqualTo(2)
        assertThat(request.content).isEqualTo("updated-content")
    }

    @Test
    fun `moveFolder maps folder keys and decrypts response`() = runTest {
        localFolderKeyDataSource.upsertKey(
            FolderKeyEntityTestFactory.create(
                folderId = "parent",
                userId = userId.id,
                shareId = shareId.id,
                keyRotation = 1,
                encryptedKey = encryptedBytes(40)
            )
        )
        remoteFolderDataSource.moveFolderResponse = FolderApiModelTestFactory.create(
            folderId = "folder-to-move",
            parentFolderId = "parent",
            keyRotation = 1,
            folderKey = "moved-folder-key"
        )
        openFolder.setOutput("folder-to-move", "Moved Name", encryptedBytes(4))

        val result = repository.moveFolder(
            userId = userId,
            shareId = shareId,
            folderId = FolderId("folder-to-move"),
            parentFolderId = FolderId("parent"),
            folderKeys = listOf(1L to "key-1", 2L to "key-2")
        )

        assertThat(result.name).isEqualTo("Moved Name")
        val request = remoteFolderDataSource.moveFolderCalls.single().request
        assertThat(request.parentFolderId).isEqualTo("parent")
        assertThat(request.folderKeys.map { it.keyRotation to it.folderKey })
            .containsExactly(1L to "key-1", 2L to "key-2")
    }

    @Test
    fun `deleteFolders sends remote request and deletes locally`() = runTest {
        repository.deleteFolders(
            userId = userId,
            shareId = shareId,
            folderIds = listOf(FolderId("f1"), FolderId("f2"))
        )

        assertThat(remoteFolderDataSource.deleteFolderCalls).hasSize(1)
        assertThat(remoteFolderDataSource.deleteFolderCalls.single().request.folderIds)
            .containsExactly("f1", "f2")
        assertThat(localFolderDataSource.deleteCalls).hasSize(1)
        assertThat(localFolderDataSource.deleteCalls.single().folderIds.map { it.id })
            .containsExactly("f1", "f2")
    }

    @Test
    fun `deleteFoldersLocally skips deletion when folder list is empty`() = runTest {
        repository.deleteFoldersLocally(
            userId = userId,
            shareId = shareId,
            folderIds = emptyList()
        )

        assertThat(localFolderDataSource.deleteCalls).isEmpty()
    }

    private fun folderEntity(
        folderId: String,
        name: String,
        parentFolderId: String?
    ) = FolderEntityTestFactory.create(
        id = folderId,
        userId = userId.id,
        shareId = shareId.id,
        parentFolderId = parentFolderId,
        encryptedContent = encryptFolderName(name)
    )

    private fun encryptFolderName(name: String): EncryptedByteArray {
        val proto = FolderV1.Folder.newBuilder().setName(name).build()
        return FakeEncryptionContext.encrypt(proto.toByteArray())
    }

    private fun decryptFolderName(content: EncryptedByteArray): String {
        val decrypted = FakeEncryptionContext.decrypt(content)
        return FolderV1.Folder.parseFrom(decrypted).name
    }

    private fun encryptedBytes(value: Int): EncryptedByteArray =
        FakeEncryptionContext.encrypt(byteArrayOf(value.toByte()))

}
