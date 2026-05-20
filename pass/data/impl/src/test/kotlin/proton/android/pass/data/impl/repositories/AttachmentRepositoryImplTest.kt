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
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.FakePendingAttachmentLinkRepository
import proton.android.pass.data.fakes.repositories.FakeUserAccessDataRepository
import proton.android.pass.data.impl.crypto.attachment.DecryptFileAttachmentChunkImpl
import proton.android.pass.data.impl.fakes.FakeContentResolver
import proton.android.pass.data.impl.fakes.FakeEncryptFileAttachmentChunk
import proton.android.pass.data.impl.fakes.FakeEncryptFileAttachmentMetadata
import proton.android.pass.data.impl.fakes.FakeFileTypeDetector
import proton.android.pass.data.impl.fakes.FakeFileUriGenerator
import proton.android.pass.data.impl.fakes.FakeGetItemKey
import proton.android.pass.data.impl.fakes.FakeLegacyAttachmentsDirProvider
import proton.android.pass.data.impl.fakes.FakeLocalAttachmentsDataSource
import proton.android.pass.data.impl.fakes.FakeReencryptAttachment
import proton.android.pass.data.impl.fakes.FakeRemoteAttachmentsDataSource
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.Chunk
import proton.android.pass.domain.attachments.ChunkId
import proton.android.pass.domain.attachments.PersistentAttachmentId
import proton.android.pass.test.MainDispatcherRule
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import kotlin.test.assertFailsWith

internal class AttachmentRepositoryImplTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var filesDir: File
    private lateinit var cacheDir: File
    private lateinit var instance: AttachmentRepositoryImpl
    private lateinit var fakeRemote: FakeRemoteAttachmentsDataSource
    private lateinit var fakeEncryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var fakeFileUriGenerator: FakeFileUriGenerator

    private val userId = UserId("test-user-id")
    private val shareId = ShareId("test-share-id")
    private val itemId = ItemId("test-item-id")
    private val attachmentId = AttachmentId("test-attachment-id")
    private val persistentId = PersistentAttachmentId("persistent-id")
    private val chunkId = ChunkId("chunk-id")

    @Before
    fun setup() {
        filesDir = tmpFolder.newFolder("files")
        cacheDir = tmpFolder.newFolder("cache")

        fakeRemote = FakeRemoteAttachmentsDataSource()
        fakeEncryptionContextProvider = FakeEncryptionContextProvider()
        fakeFileUriGenerator = FakeFileUriGenerator(filesDir, cacheDir)

        instance = AttachmentRepositoryImpl(
            contentResolver = FakeContentResolver(),
            legacyAttachmentsDirProvider = FakeLegacyAttachmentsDirProvider(
                tmpFolder.root.resolve("legacy_attachments")
            ),
            appDispatchers = FakeAppDispatchers(),
            remote = fakeRemote,
            local = FakeLocalAttachmentsDataSource(),
            encryptionContextProvider = fakeEncryptionContextProvider,
            pendingAttachmentLinkRepository = FakePendingAttachmentLinkRepository(),
            fileTypeDetector = FakeFileTypeDetector(),
            fileUriGenerator = fakeFileUriGenerator,
            reencryptAttachment = FakeReencryptAttachment(),
            userAccessDataRepository = FakeUserAccessDataRepository(),
            getItemKey = FakeGetItemKey(),
            encryptFileAttachmentMetadata = FakeEncryptFileAttachmentMetadata(),
            encryptFileAttachmentChunk = FakeEncryptFileAttachmentChunk(),
            decryptFileAttachmentChunk = DecryptFileAttachmentChunkImpl()
        )
    }

    @Test
    fun `cached file on disk is encrypted not plaintext`() = runTest {
        // Arrange
        val plaintext = "test file content".toByteArray()
        val remoteEncryptedChunk = plaintext + byteArrayOf(0xCA.toByte(), 0xFE.toByte())

        fakeRemote.chunksToReturn = mapOf(chunkId to EncryptedByteArray(remoteEncryptedChunk))

        val attachment = createTestAttachment(listOf(chunkId))

        // Act
        instance.downloadAttachment(userId, attachment)

        // Assert
        val encryptedCacheFile = fakeFileUriGenerator.encryptedCacheFileFor(userId, attachment)
        assertThat(encryptedCacheFile.exists()).isTrue()
        val diskBytes = encryptedCacheFile.readBytes()

        // Disk format: [4-byte BE length][locally-encrypted chunk] per remote chunk.
        // FakeEncryptionContext.encrypt appends [0xCA, 0xFE].
        val encryptedChunk = plaintext + byteArrayOf(0xCA.toByte(), 0xFE.toByte())
        val expectedDiskBytes = ByteArrayOutputStream().also { baos ->
            DataOutputStream(baos).use { dos ->
                dos.writeInt(encryptedChunk.size)
                dos.write(encryptedChunk)
            }
        }.toByteArray()
        assertThat(diskBytes).isEqualTo(expectedDiskBytes)
        assertThat(diskBytes).isNotEqualTo(plaintext)
    }

    @Test
    fun `cache hit skips network download`() = runTest {
        // Arrange
        val plaintext = "test file content".toByteArray()
        val encryptedChunk = plaintext + byteArrayOf(0xCA.toByte(), 0xFE.toByte())
        val cacheBytes = ByteArrayOutputStream().also { baos ->
            DataOutputStream(baos).use { dos ->
                dos.writeInt(encryptedChunk.size)
                dos.write(encryptedChunk)
            }
        }.toByteArray()

        val encryptedCacheFile =
            fakeFileUriGenerator.encryptedCacheFileFor(userId, createTestAttachment(emptyList()))
        encryptedCacheFile.parentFile?.mkdirs()
        encryptedCacheFile.writeBytes(cacheBytes)

        val attachment = createTestAttachment(listOf(chunkId))

        // Act
        instance.downloadAttachment(userId, attachment)

        // Assert
        assertThat(fakeRemote.downloadChunkInvocations).isEqualTo(0)
    }

    @Test
    fun `network failure deletes encrypted cache file`() = runTest {
        // Arrange
        fakeRemote.shouldThrowOnDownload = RuntimeException("network error")

        val attachment = createTestAttachment(listOf(chunkId))
        val encryptedCacheFile = fakeFileUriGenerator.encryptedCacheFileFor(userId, attachment)

        encryptedCacheFile.parentFile?.mkdirs()

        // Act & Assert
        assertFailsWith<RuntimeException> {
            instance.downloadAttachment(userId, attachment)
        }

        // Cache file should be deleted after failure
        assertThat(encryptedCacheFile.exists()).isFalse()
    }

    @Test
    fun `multi-chunk download writes all chunks to disk in index order`() = runTest {
        val chunkId0 = ChunkId("chunk-0")
        val chunkId1 = ChunkId("chunk-1")
        val chunkId2 = ChunkId("chunk-2")
        val plaintext0 = "first chunk content".toByteArray()
        val plaintext1 = "second chunk payload here".toByteArray()
        val plaintext2 = "third".toByteArray()
        val trail = byteArrayOf(0xCA.toByte(), 0xFE.toByte())

        fakeRemote.chunksToReturn = mapOf(
            chunkId0 to EncryptedByteArray(plaintext0 + trail),
            chunkId1 to EncryptedByteArray(plaintext1 + trail),
            chunkId2 to EncryptedByteArray(plaintext2 + trail)
        )
        val attachment = createTestAttachment(listOf(chunkId0, chunkId1, chunkId2))

        instance.downloadAttachment(userId, attachment)

        val diskBytes = fakeFileUriGenerator.encryptedCacheFileFor(userId, attachment).readBytes()
        val expected = ByteArrayOutputStream().also { baos ->
            DataOutputStream(baos).use { dos ->
                for (plaintext in listOf(plaintext0, plaintext1, plaintext2)) {
                    val encrypted = plaintext + trail
                    dos.writeInt(encrypted.size)
                    dos.write(encrypted)
                }
            }
        }.toByteArray()
        assertThat(diskBytes).isEqualTo(expected)
        assertThat(fakeRemote.downloadChunkInvocations).isEqualTo(3)
    }

    @Test
    fun `round-trip decryption of multi-chunk file produces original plaintext`() = runTest {
        val chunkId0 = ChunkId("chunk-0")
        val chunkId1 = ChunkId("chunk-1")
        val plaintext0 = "hello from chunk zero ".repeat(500).toByteArray() // ~11 KB
        val plaintext1 = "data in chunk one here ".repeat(500).toByteArray()
        val trail = byteArrayOf(0xCA.toByte(), 0xFE.toByte())

        fakeRemote.chunksToReturn = mapOf(
            chunkId0 to EncryptedByteArray(plaintext0 + trail),
            chunkId1 to EncryptedByteArray(plaintext1 + trail)
        )
        val attachment = createTestAttachment(listOf(chunkId0, chunkId1))

        instance.downloadAttachment(userId, attachment)

        val encryptedCacheFile = fakeFileUriGenerator.encryptedCacheFileFor(userId, attachment)
        val decryptedOutput = ByteArrayOutputStream()
        DataInputStream(encryptedCacheFile.inputStream().buffered()).use { dis ->
            fakeEncryptionContextProvider.withEncryptionContext {
                try {
                    while (true) {
                        val len = dis.readInt()
                        val encBytes = ByteArray(len)
                        dis.readFully(encBytes)
                        decryptedOutput.write(decrypt(EncryptedByteArray(encBytes)))
                    }
                } catch (_: EOFException) {
                }
            }
        }

        assertThat(decryptedOutput.toByteArray()).isEqualTo(plaintext0 + plaintext1)
    }

    private fun createTestAttachment(chunkIds: List<ChunkId>): Attachment {
        val chunks = chunkIds.mapIndexed { index, cid ->
            Chunk(id = cid, size = 100L, index = index)
        }
        return Attachment(
            id = attachmentId,
            persistentId = persistentId,
            shareId = shareId,
            itemId = itemId,
            name = "test.txt",
            mimeType = "text/plain",
            type = AttachmentType.Document,
            size = 100L,
            createTime = Instant.fromEpochSeconds(1000),
            modifyTime = Instant.fromEpochSeconds(1000),
            revisionAdded = 1,
            revisionRemoved = null,
            reencryptedKey = EncryptedByteArray(
                ByteArray(32) { it.toByte() } + byteArrayOf(0xCA.toByte(), 0xFE.toByte())
            ),
            chunks = chunks,
            encryptionVersion = 1
        )
    }
}
