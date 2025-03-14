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
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import fileMetadata
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.MimeType
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.crypto.GetItemKey
import proton.android.pass.data.api.errors.FileSizeExceededError
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.impl.crypto.ReencryptAttachment
import proton.android.pass.data.impl.crypto.ReencryptedKey
import proton.android.pass.data.impl.crypto.ReencryptedMetadata
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity
import proton.android.pass.data.impl.db.entities.attachments.AttachmentWithChunks
import proton.android.pass.data.impl.db.entities.attachments.ChunkEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.attachments.LocalAttachmentsDataSource
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSource
import proton.android.pass.data.impl.responses.attachments.ChunkApiModel
import proton.android.pass.data.impl.responses.attachments.FileApiModel
import proton.android.pass.data.impl.util.PaginatedResponse
import proton.android.pass.data.impl.util.fetchAllPaginated
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.Chunk
import proton.android.pass.domain.attachments.ChunkId
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.domain.attachments.PendingAttachmentId
import proton.android.pass.domain.attachments.PersistentAttachmentId
import proton.android.pass.files.api.FileType
import proton.android.pass.files.api.FileUriGenerator
import proton.android.pass.log.api.PassLogger
import java.io.File
import java.net.URI
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

class AttachmentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers,
    private val remote: RemoteAttachmentsDataSource,
    private val local: LocalAttachmentsDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val pendingAttachmentLinkRepository: PendingAttachmentLinkRepository,
    private val fileTypeDetector: FileTypeDetector,
    private val fileUriGenerator: FileUriGenerator,
    private val reencryptAttachment: ReencryptAttachment,
    private val userAccessDataRepository: UserAccessDataRepository,
    private val getItemKey: GetItemKey
) : AttachmentRepository {

    override suspend fun createPendingAttachment(userId: UserId, metadata: FileMetadata): PendingAttachmentId {
        val userAccessData = userAccessDataRepository.observe(userId).first()
            ?: throw IllegalStateException("Cannot retrieve user access data")
        if (metadata.size >= userAccessData.storageMaxFileSize) {
            throw FileSizeExceededError()
        }
        val fileMetadata = fileMetadata {
            this.name = metadata.name
            this.mimeType = metadata.mimeType
        }

        val fileKey = EncryptionKey.generate()
        val encryptedMetadata =
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey.clone()) {
                encrypt(fileMetadata.toByteArray(), EncryptionTag.FileData)
            }
        val encodedMetadata = Base64.encodeBase64String(encryptedMetadata.array)
        val chunks = ceil(metadata.size.toDouble() / CHUNK_SIZE).toInt()
        val id = remote.createPendingFile(userId, encodedMetadata, chunks)
            .let(::PendingAttachmentId)
        pendingAttachmentLinkRepository.addToLink(id, fileKey)
        return id
    }

    override suspend fun updatePendingAttachment(
        userId: UserId,
        attachmentId: PendingAttachmentId,
        metadata: FileMetadata
    ) {
        val fileMetadata = fileMetadata {
            this.name = metadata.name
            this.mimeType = metadata.mimeType
        }
        val fileKey: EncryptionKey = pendingAttachmentLinkRepository.getToLinkKey(attachmentId)
            ?: throw IllegalStateException("No encryption key found for attachment")
        val encryptedMetadata =
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey.clone()) {
                encrypt(fileMetadata.toByteArray(), EncryptionTag.FileData)
            }
        val encodedMetadata = Base64.encodeBase64String(encryptedMetadata.array)
        remote.updatePendingFile(
            userId = userId,
            pendingAttachmentId = attachmentId,
            metadata = encodedMetadata
        )
    }

    @Suppress("LongMethod")
    override suspend fun uploadPendingAttachment(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        uri: URI
    ) {
        val fileKey: EncryptionKey =
            pendingAttachmentLinkRepository.getToLinkKey(pendingAttachmentId)
                ?: throw IllegalStateException("No encryption key found for attachment $pendingAttachmentId")
        val contentUri: Uri = uri.toString().toUri()
        withContext(appDispatchers.io) {
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey) {
                context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var chunkIndex = 0
                    var bytesRead: Int

                    while (isActive) {
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) break

                        PassLogger.d(
                            TAG,
                            "Uploading chunk $chunkIndex for attachment $pendingAttachmentId"
                        )
                        remote.uploadPendingFile(
                            userId = userId,
                            pendingAttachmentId = pendingAttachmentId,
                            chunkIndex = chunkIndex,
                            encryptedByteArray = encrypt(
                                buffer.copyOf(bytesRead),
                                EncryptionTag.FileData
                            )
                        )
                        chunkIndex++
                    }
                } ?: throw IllegalStateException("Unable to open input stream for URI: $contentUri")
            }
        }
    }

    override suspend fun linkPendingAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Long,
        toLink: Map<PendingAttachmentId, EncryptionKey>,
        toUnlink: Set<AttachmentId>
    ) {
        val encryptedItemKey = getItemKey(userId, shareId, itemId)
        val itemKey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(encryptedItemKey.key))
        }
        val toLinkEncrypted = encryptionContextProvider.withEncryptionContextSuspendable(itemKey) {
            toLink.mapValues { (_, fileKey) ->
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
                local.removeAttachmentsById(
                    shareId = shareId,
                    itemId = itemId,
                    attachmentIdList = batchedToUnlink.getOrNull(i) ?: emptyList()
                )
            }
        }
    }

    override suspend fun updateFileMetadata(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        title: String
    ) {
        withContext(appDispatchers.io) {
            val attachment = local.getAttachmentById(shareId, itemId, attachmentId)
                ?: throw IllegalStateException("Attachment not found")
            val (decryptedFileKey, decryptedMetadata) =
                encryptionContextProvider.withEncryptionContextSuspendable {
                    decrypt(attachment.reencryptedKey) to decrypt(attachment.reencryptedMetadata)
                }
            val metadata = FileV1.FileMetadata.parseFrom(decryptedMetadata)
            val updatedMetadata = metadata.toBuilder().setName(title).build()
            val encryptedMetadata = encryptionContextProvider.withEncryptionContextSuspendable(
                EncryptionKey(decryptedFileKey)
            ) { encrypt(updatedMetadata.toByteArray(), EncryptionTag.FileData) }
            val encodedMetadata = Base64.encodeBase64String(encryptedMetadata.array)

            remote.updateFileMetadata(
                userId = userId,
                shareId = shareId,
                itemId = itemId,
                attachmentId = attachmentId,
                metadata = encodedMetadata
            )
            val encryptedItemKey = getItemKey(userId, shareId, itemId)

            val (reencryptedMetadatas, reencryptedKeys) = reencryptAttachment(
                encryptedItemKey = encryptedItemKey.key,
                encryptedMetadatas = listOf(encodedMetadata),
                encryptedKeys = listOf(attachment.key)
            )
            val entity = attachment.copy(
                reencryptedKey = reencryptedKeys.first().value,
                reencryptedMetadata = reencryptedMetadatas.first().value
            )
            local.updateAttachment(entity)
        }
    }

    override suspend fun restoreOldFile(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ) {
        withContext(appDispatchers.io) {
            val previousEntity = local.getAttachmentById(shareId, itemId, attachmentId)
                ?: throw IllegalStateException("Attachment not found")

            val (_, fileResponse) = remote.restoreOldFile(
                userId = userId,
                shareId = shareId,
                itemId = itemId,
                attachmentId = AttachmentId(previousEntity.id),
                itemKeyRotation = previousEntity.itemKeyRotation.toInt(),
                fileKey = previousEntity.key
            )

            val updatedEntity = fileResponse.toEntity(
                userId = userId,
                shareId = shareId,
                itemId = itemId,
                reencryptedKey = previousEntity.key.toEncryptedByteArray(),
                reencryptedMetadata = previousEntity.reencryptedMetadata
            )
            local.updateAttachment(updatedEntity)
        }
    }

    override fun observeActiveAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>> = local.observeActiveAttachmentsWithChunksForItem(shareId, itemId)
        .map { attachmentsWithChunks ->
            mapAttachmentsWithChunksToDomain(attachmentsWithChunks, shareId, itemId)
        }
        .onStart {
            coroutineScope {
                launch {
                    runCatching { refreshActiveAttachments(userId, shareId, itemId) }
                        .onSuccess { PassLogger.i(TAG, "Refreshed attachments") }
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

    override fun observeAttachmentsForAllRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>> = local.observeAllAttachmentsWithChunksForItemRevisions(shareId, itemId)
        .map { attachmentsWithChunks ->
            mapAttachmentsWithChunksToDomain(attachmentsWithChunks, shareId, itemId)
        }
        .onStart {
            coroutineScope {
                launch {
                    runCatching { refreshAttachmentsForAllRevisions(userId, shareId, itemId) }
                        .onSuccess { PassLogger.i(TAG, "Refreshed attachments") }
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

    override suspend fun getAttachmentById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Attachment = withContext(appDispatchers.io) {
        val attachment = local.getAttachmentById(shareId, itemId, attachmentId)
            ?: throw IllegalStateException("Attachment not found")
        val chunks = local.getChunksForAttachment(shareId, itemId, attachmentId)
        encryptionContextProvider.withEncryptionContextSuspendable {
            attachment.toDomain(
                encryptionContext = this,
                fileTypeDetector = fileTypeDetector,
                shareId = shareId,
                itemId = itemId,
                chunks = chunks.map(ChunkEntity::toDomain)
            )
        }
    }

    private suspend fun mapAttachmentsWithChunksToDomain(
        attachmentsWithChunks: List<AttachmentWithChunks>,
        shareId: ShareId,
        itemId: ItemId
    ) = encryptionContextProvider.withEncryptionContextSuspendable {
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

    private suspend fun refreshActiveAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        fetchAllPaginated(
            fetchPage = { lastToken ->
                val response = remote.retrieveActiveFiles(userId, shareId, itemId, lastToken)
                PaginatedResponse(
                    items = response.files,
                    total = response.total,
                    lastId = response.lastId
                )
            },
            mapToDomain = { it },
            storeResults = { attachments ->
                saveRetrievedAttachments(
                    userId = userId,
                    shareId = shareId,
                    itemId = itemId,
                    fileDetails = attachments
                )
            }
        )
    }

    private suspend fun refreshAttachmentsForAllRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        fetchAllPaginated(
            fetchPage = { lastToken ->
                val response =
                    remote.retrieveFilesForAllRevisions(userId, shareId, itemId, lastToken)
                PaginatedResponse(
                    items = response.files,
                    total = response.total,
                    lastId = response.lastId
                )
            },
            mapToDomain = { it },
            storeResults = { attachments ->
                saveRetrievedAttachments(
                    userId = userId,
                    shareId = shareId,
                    itemId = itemId,
                    fileDetails = attachments
                )
            }
        )
    }

    private suspend fun AttachmentRepositoryImpl.saveRetrievedAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        fileDetails: List<FileApiModel>
    ) {
        val encryptedItemKey = getItemKey(userId, shareId, itemId)

        val (reencryptedMetadatas, reencryptedKeys) = reencryptAttachment(
            encryptedItemKey.key,
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
            persistentId = attachment.persistentId
        )
        val directory = fileUriGenerator.getDirectoryForFileType(fileType)
        val existingFileUri = withContext(appDispatchers.io) {
            val file = File(directory, attachment.persistentId.id)
            if (file.exists() && file.length() != 0L) {
                fileUriGenerator.getFileProviderUri(file)
            } else null
        }
        if (existingFileUri != null) return existingFileUri

        val uri: URI = fileUriGenerator.generate(fileType)
        val contentUri = uri.toString().toUri()

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
    persistentId = this.persistentFileId,
    id = this.fileId,
    shareId = shareId.id,
    itemId = itemId.id,
    metadata = this.metadata,
    size = this.size,
    createTime = Instant.fromEpochSeconds(this.createTime),
    modifyTime = Instant.fromEpochSeconds(this.modifyTime),
    key = this.fileKey,
    itemKeyRotation = this.itemKeyRotation,
    revisionAdded = this.revisionAdded,
    revisionRemoved = this.revisionRemoved,
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
        persistentId = PersistentAttachmentId(this.persistentId),
        shareId = shareId,
        itemId = itemId,
        name = metadata.name,
        mimeType = metadata.mimeType,
        type = fileType.toDomain(),
        size = this.size,
        createTime = this.createTime,
        modifyTime = this.modifyTime ?: this.createTime,
        reencryptedKey = this.reencryptedKey,
        revisionAdded = this.revisionAdded,
        revisionRemoved = this.revisionRemoved,
        chunks = chunks
    )
}

fun ChunkEntity.toDomain(): Chunk = Chunk(
    id = ChunkId(this.id),
    size = this.size,
    index = this.index
)
