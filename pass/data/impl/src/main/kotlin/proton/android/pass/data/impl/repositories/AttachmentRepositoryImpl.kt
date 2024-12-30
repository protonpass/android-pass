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

package proton.android.pass.data.impl.repositories

import FileV1
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import fileMetadata
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.MimeType
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.data.api.errors.ItemKeyNotAvailableError
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.impl.crypto.ReencryptAttachment
import proton.android.pass.data.impl.crypto.ReencryptedKey
import proton.android.pass.data.impl.crypto.ReencryptedMetadata
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity
import proton.android.pass.data.impl.db.entities.attachments.ChunkEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.local.attachments.LocalAttachmentsDataSource
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSource
import proton.android.pass.data.impl.responses.attachments.ChunkApiModel
import proton.android.pass.data.impl.responses.attachments.FileApiModel
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.Chunk
import proton.android.pass.domain.attachments.ChunkId
import proton.android.pass.files.api.FileType
import proton.android.pass.files.api.FileUriGenerator
import proton.android.pass.log.api.PassLogger
import java.io.File
import java.net.URI
import javax.inject.Inject
import kotlin.math.max

class AttachmentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers,
    private val metadataResolver: MetadataResolver,
    private val remote: RemoteAttachmentsDataSource,
    private val local: LocalAttachmentsDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val pendingAttachmentLinkRepository: PendingAttachmentLinkRepository,
    private val fileTypeDetector: FileTypeDetector,
    private val localItemDataSource: LocalItemDataSource,
    private val fileUriGenerator: FileUriGenerator,
    private val reencryptAttachment: ReencryptAttachment
) : AttachmentRepository {

    override suspend fun createPendingAttachment(userId: UserId, uri: URI): AttachmentId {
        val metadata = metadataResolver.extractMetadata(uri)
            ?: throw IllegalStateException("Metadata not available for URI: $uri")
        val fileMetadata: FileV1.FileMetadata = fileMetadata {
            this.name = metadata.name
            this.mimeType = metadata.mimeType
        }
        val fileKey = EncryptionKey.generate()
        val encryptedMetadata =
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey.clone()) {
                encrypt(fileMetadata.toByteArray(), EncryptionTag.FileData)
            }
        val encodedMetadata = Base64.encodeBase64String(encryptedMetadata.array)
        val id = remote.createPendingFile(userId, encodedMetadata).let(::AttachmentId)
        pendingAttachmentLinkRepository.addToLink(id, fileKey)
        return id
    }

    @Suppress("LongMethod")
    override suspend fun uploadPendingAttachment(
        userId: UserId,
        attachmentId: AttachmentId,
        uri: URI
    ) {
        val fileKey: EncryptionKey = pendingAttachmentLinkRepository.getToLinkKey(attachmentId)
            ?: throw IllegalStateException("No encryption key found for attachment $attachmentId")
        val contentUri: Uri = Uri.parse(uri.toString())

        runCatching {
            withContext(appDispatchers.io) {
                context.contentResolver.openInputStream(contentUri)
                    ?.buffered()
                    ?.use { inputStream ->
                        encryptionContextProvider.withEncryptionContextSuspendable(fileKey) {
                            val buffer = ByteArray(CHUNK_SIZE)
                            var chunkIndex = 0
                            var bytesInBuffer = inputStream.read(buffer, 0, CHUNK_SIZE)
                            if (bytesInBuffer == -1) throw IllegalStateException("File is empty")

                            if (bytesInBuffer < CHUNK_SIZE && bytesInBuffer < MIN_CHUNK_SIZE) {
                                val encryptedChunk = encrypt(
                                    buffer.copyOf(bytesInBuffer),
                                    EncryptionTag.FileData
                                )
                                remote.uploadPendingFile(
                                    userId,
                                    attachmentId,
                                    chunkIndex,
                                    encryptedChunk
                                )
                                PassLogger.d(TAG, "Finished uploading single chunk with attachment $attachmentId")
                                return@withEncryptionContextSuspendable
                            }

                            while (isActive) {
                                val bytesRead =
                                    inputStream.read(
                                        buffer,
                                        bytesInBuffer,
                                        CHUNK_SIZE - bytesInBuffer
                                    )
                                if (bytesRead == -1) break

                                bytesInBuffer += bytesRead

                                if (bytesInBuffer >= CHUNK_SIZE) {
                                    PassLogger.d(TAG, "Uploading chunk $chunkIndex for attachment $attachmentId")
                                    val encryptedChunk = encrypt(
                                        buffer.copyOf(CHUNK_SIZE),
                                        EncryptionTag.FileData
                                    )
                                    remote.uploadPendingFile(
                                        userId = userId,
                                        attachmentId = attachmentId,
                                        chunkIndex = chunkIndex,
                                        encryptedByteArray = encryptedChunk
                                    )
                                    chunkIndex++

                                    System.arraycopy(
                                        buffer,
                                        CHUNK_SIZE,
                                        buffer,
                                        0,
                                        bytesInBuffer - CHUNK_SIZE
                                    )
                                    bytesInBuffer -= CHUNK_SIZE
                                }
                            }

                            if (bytesInBuffer > 0) {
                                if (bytesInBuffer < MIN_CHUNK_SIZE && chunkIndex > 0) {
                                    PassLogger.d(TAG, "Combining last chunk with previous for attachment $attachmentId")
                                    val previousChunk = buffer.copyOf(bytesInBuffer + CHUNK_SIZE)
                                    remote.uploadPendingFile(
                                        userId = userId,
                                        attachmentId = attachmentId,
                                        chunkIndex = chunkIndex - 1,
                                        encryptedByteArray = encrypt(
                                            previousChunk,
                                            EncryptionTag.FileData
                                        )
                                    )
                                } else {
                                    PassLogger.d(TAG, "Uploading last chunk for attachment $attachmentId")
                                    val encryptedChunk =
                                        encrypt(
                                            buffer.copyOf(bytesInBuffer),
                                            EncryptionTag.FileData
                                        )
                                    remote.uploadPendingFile(
                                        userId = userId,
                                        attachmentId = attachmentId,
                                        chunkIndex = chunkIndex,
                                        encryptedByteArray = encryptedChunk
                                    )
                                }
                            }
                        }
                    }
                    ?: throw IllegalStateException("Unable to open input stream for URI: $contentUri")
            }
        }.onFailure {
            pendingAttachmentLinkRepository.removeToLink(attachmentId)
            throw it
        }
    }

    override suspend fun linkPendingAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Long,
        toLink: Map<AttachmentId, EncryptionKey>,
        toUnlink: Set<AttachmentId>
    ) {
        val encryptedItemKey = localItemDataSource.getById(shareId, itemId)?.encryptedKey
            ?: throw ItemKeyNotAvailableError()
        val itemKey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(encryptedItemKey))
        }
        val mappings: Map<AttachmentId, EncryptionKey> = pendingAttachmentLinkRepository.getAllToLink()
        val toLinkEncrypted: Map<AttachmentId, EncryptedString> =
            encryptionContextProvider.withEncryptionContextSuspendable(itemKey) {
                mappings.mapValues { (_, fileKey) ->
                    val encryptedKey = encrypt(fileKey.value(), EncryptionTag.FileKey)
                    Base64.encodeBase64String(encryptedKey.array)
                }
            }
        val batchedToLinkEncrypted = toLinkEncrypted.entries.chunked(TO_LINK_BATCH_SIZE)
            .map { list -> list.associate { item -> item.key to item.value } }
        val batchedToUnlink = toUnlink.chunked(TO_UNLINK_BATCH_SIZE)
        val iterations = max(batchedToLinkEncrypted.size, batchedToUnlink.size)
        for (i in 0 until iterations) {
            remote.linkPendingFiles(
                userId = userId,
                shareId = shareId,
                itemId = itemId,
                revision = revision,
                filesToAdd = batchedToLinkEncrypted.getOrNull(i) ?: emptyMap(),
                filesToRemove = batchedToUnlink.getOrNull(i)?.toSet() ?: emptySet()
            )
            withContext(appDispatchers.io) {
                local.removeAttachmentById(
                    shareId = shareId,
                    itemId = itemId,
                    attachmentIdList = batchedToUnlink.getOrNull(i) ?: emptyList()
                )
            }
        }
    }

    override fun observeAllAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>> = local.observeAttachmentsWithChunksForItem(shareId, itemId)
        .map { attachmentsWithChunks ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                attachmentsWithChunks.map {
                    it.attachment.toDomain(
                        encryptionContext = this,
                        fileTypeDetector = fileTypeDetector,
                        shareId = shareId,
                        itemId = itemId,
                        chunks = it.chunks.map(ChunkEntity::toDomain)
                    )
                }
            }
        }
        .onStart {
            coroutineScope {
                launch {
                    runCatching { refreshAttachments(userId, shareId, itemId) }
                        .onFailure {
                            PassLogger.i(
                                TAG,
                                "Failed to refresh attachments for item " +
                                    "with share ${shareId.id} and item ${itemId.id}"
                            )
                        }
                }
            }
        }

    private suspend fun refreshAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        val fileDetails = remote.retrieveActiveFiles(
            userId = userId,
            shareId = shareId,
            itemId = itemId
        ).files

        val encryptedItemKey = localItemDataSource.getById(shareId, itemId)?.encryptedKey
            ?: throw ItemKeyNotAvailableError()

        val (reencryptedMetadatas, reencryptedKeys) = reencryptAttachment(
            encryptedItemKey,
            fileDetails.map(FileApiModel::metadata),
            fileDetails.map(FileApiModel::fileKey)
        )
        val attachmentsWithChunks = fileDetails
            .zip(reencryptedMetadatas)
            .zip(reencryptedKeys)
            .associate { (pair, key: ReencryptedKey) ->
                val (fileDetail, metadata: ReencryptedMetadata) = pair
                val attachmentEntity = fileDetail.toEntity(
                    userId = userId,
                    shareId = shareId,
                    itemId = itemId,
                    reencryptedKey = key.value,
                    reencryptedMetadata = metadata.value
                )
                val chunkEntities = fileDetail.chunks.map {
                    it.toChunkEntity(fileDetail.fileId, itemId, shareId)
                }
                attachmentEntity to chunkEntities
            }
        local.saveAttachmentsWithChunks(
            attachmentEntities = attachmentsWithChunks.keys.toList(),
            chunkEntities = attachmentsWithChunks.values.flatten()
        )
    }

    override suspend fun downloadAttachment(userId: UserId, attachment: Attachment): URI {
        if (attachment.chunks.isEmpty()) throw IllegalStateException("No chunks provided")

        val fileType = FileType.ItemAttachment(
            userId = userId,
            shareId = attachment.shareId,
            itemId = attachment.itemId,
            attachmentId = attachment.id
        )
        val directory = fileUriGenerator.getDirectoryForFileType(fileType)
        val existingFileUri = withContext(appDispatchers.io) {
            val file = File(directory, attachment.id.id)
            if (file.exists() && file.length() != 0L) {
                fileUriGenerator.getFileProviderUri(file)
            } else null
        }
        if (existingFileUri != null) return existingFileUri

        val uri: URI = fileUriGenerator.generate(fileType)
        val contentUri = Uri.parse(uri.toString())

        withContext(appDispatchers.io) {
            runCatching {
                context.contentResolver.openOutputStream(contentUri)?.buffered()
                    ?.use { outputStream ->
                        val fileKey = encryptionContextProvider.withEncryptionContextSuspendable {
                            EncryptionKey(decrypt(attachment.reencryptedKey))
                        }

                        encryptionContextProvider.withEncryptionContextSuspendable(fileKey) {
                            attachment.chunks.sortedBy { it.index }.forEach { chunk ->
                                val encryptedChunk = remote.downloadChunk(
                                    userId = userId,
                                    shareId = attachment.shareId,
                                    itemId = attachment.itemId,
                                    attachmentId = attachment.id,
                                    chunkId = chunk.id
                                )
                                decrypt(encryptedChunk, EncryptionTag.FileData).inputStream()
                                    .use { decryptedStream ->
                                        decryptedStream.copyTo(outputStream)
                                    }
                            }
                        }
                    }
                    ?: throw IllegalStateException("Unable to open output stream for URI: $contentUri")
            }.onFailure {
                context.contentResolver.delete(contentUri, null, null)
                throw it
            }
        }

        return URI.create(contentUri.toString())
    }

    companion object {
        private const val CHUNK_SIZE = 10 * 1024 * 1024 // 10 MB
        private const val MIN_CHUNK_SIZE = 1 * 1024 * 1024 // 1 MB

        private const val TO_LINK_BATCH_SIZE = 10
        private const val TO_UNLINK_BATCH_SIZE = 100

        private const val TAG = "AttachmentRepositoryImpl"
    }
}

@Suppress("LongParameterList")
fun FileApiModel.toEntity(
    userId: UserId,
    shareId: ShareId,
    itemId: ItemId,
    reencryptedKey: EncryptedByteArray,
    reencryptedMetadata: EncryptedByteArray
): AttachmentEntity = AttachmentEntity(
    userId = userId.id,
    id = this.fileId,
    shareId = shareId.id,
    itemId = itemId.id,
    metadata = this.metadata,
    size = this.size,
    createTime = Instant.fromEpochSeconds(this.createTime),
    key = this.fileKey,
    itemKeyRotation = this.itemKeyRotation,
    reencryptedKey = reencryptedKey,
    reencryptedMetadata = reencryptedMetadata
)

fun ChunkApiModel.toChunkEntity(
    attachmentId: String,
    itemId: ItemId,
    shareId: ShareId
): ChunkEntity = ChunkEntity(
    id = this.chunkId,
    attachmentId = attachmentId,
    itemId = itemId.id,
    shareId = shareId.id,
    size = this.size,
    index = this.index
)

fun AttachmentEntity.toDomain(
    encryptionContext: EncryptionContext,
    fileTypeDetector: FileTypeDetector,
    shareId: ShareId,
    itemId: ItemId,
    chunks: List<Chunk>
): Attachment {
    val decryptedMetadata = encryptionContext.decrypt(this.reencryptedMetadata)
    val metadata = FileV1.FileMetadata.parseFrom(decryptedMetadata)
    val fileType = fileTypeDetector.getFileTypeFromMimeType(MimeType(metadata.mimeType))
    return Attachment(
        id = AttachmentId(this.id),
        shareId = shareId,
        itemId = itemId,
        name = metadata.name,
        mimeType = metadata.mimeType,
        type = fileType.toDomain(),
        size = this.size,
        createTime = this.createTime,
        reencryptedKey = this.reencryptedKey,
        chunks = chunks
    )
}

fun ChunkEntity.toDomain(): Chunk = Chunk(
    id = ChunkId(this.id),
    size = this.size,
    index = this.index
)
