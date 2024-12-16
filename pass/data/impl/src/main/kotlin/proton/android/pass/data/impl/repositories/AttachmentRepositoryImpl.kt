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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.MimeType
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.data.api.errors.ItemKeyNotAvailableError
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity
import proton.android.pass.data.impl.db.entities.attachments.ChunkEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.local.attachments.LocalAttachmentsDataSource
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSource
import proton.android.pass.data.impl.responses.attachments.FileDetailsResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.AttachmentKey
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.Chunk
import proton.android.pass.domain.attachments.ChunkId
import proton.android.pass.files.api.FileType
import proton.android.pass.files.api.FileUriGenerator
import proton.android.pass.log.api.PassLogger
import java.io.File
import java.net.URI
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers,
    private val metadataResolver: MetadataResolver,
    private val remote: RemoteAttachmentsDataSource,
    private val local: LocalAttachmentsDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val fileKeyRepository: FileKeyRepository,
    private val fileTypeDetector: FileTypeDetector,
    private val localItemDataSource: LocalItemDataSource,
    private val fileUriGenerator: FileUriGenerator
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
        fileKeyRepository.addMapping(id, fileKey)
        return id
    }

    override suspend fun uploadPendingAttachment(
        userId: UserId,
        attachmentId: AttachmentId,
        uri: URI
    ) {
        val fileKey: EncryptionKey = fileKeyRepository.getEncryptionKey(attachmentId)
            ?: throw IllegalStateException("No encryption key found for attachment $attachmentId")
        val contentUri: Uri = Uri.parse(uri.toString())
        val encryptedByteArray = withContext(appDispatchers.io) {
            context.contentResolver.openInputStream(contentUri)?.use { inputSteam ->
                encryptionContextProvider.withEncryptionContextSuspendable(fileKey) {
                    encrypt(inputSteam.readBytes(), EncryptionTag.FileData)
                }
            } ?: throw IllegalStateException("Unable to open input stream for URI: $contentUri")
        }
        remote.uploadPendingFile(userId, attachmentId, encryptedByteArray)
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
        val mappings: Map<AttachmentId, EncryptionKey> = fileKeyRepository.getAllMappings()
        val encryptedContents: Map<AttachmentId, EncryptedString> =
            encryptionContextProvider.withEncryptionContextSuspendable(itemKey) {
                mappings.mapValues { (_, fileKey) ->
                    val encryptedKey = encrypt(fileKey.value(), EncryptionTag.FileKey)
                    Base64.encodeBase64String(encryptedKey.array)
                }
            }
        remote.linkPendingFiles(userId, shareId, itemId, revision, encryptedContents, toUnlink)
    }

    override fun observeAllAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>> = local.observeAttachmentsWithChunksForItem(shareId, itemId)
        .map { attachmentsWithChunks ->
            attachmentsWithChunks.map { attachmentWithChunks ->
                attachmentWithChunks.attachment.toDomain(
                    shareId = shareId,
                    itemId = itemId,
                    chunks = attachmentWithChunks.chunks.map(ChunkEntity::toDomain)
                )
            }
        }
        .onStart {
            coroutineScope {
                launch {
                    runCatching { refreshAttachments(userId, shareId, itemId) }
                        .onError {
                            PassLogger.i(
                                TAG,
                                "Failed to refresh attachments for item " +
                                    "with share $shareId and item $itemId"
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
        val responseMap: Map<String, FileDetailsResponse> = remote.retrieveAllFiles(
            userId = userId,
            shareId = shareId,
            itemId = itemId
        ).files.associateBy { it.fileId }
        val encryptedItemKey = localItemDataSource.getById(shareId, itemId)?.encryptedKey
            ?: throw ItemKeyNotAvailableError()
        val itemKey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(encryptedItemKey))
        }
        val keyMap = encryptionContextProvider.withEncryptionContextSuspendable(itemKey) {
            responseMap.mapValues { file ->
                val decodedFileKey = Base64.decodeBase64(file.value.fileKey)
                val decryptedKey =
                    decrypt(EncryptedByteArray(decodedFileKey), EncryptionTag.FileKey)
                EncryptionKey(decryptedKey)
            }
        }
        val attachments = responseMap.map {
            val fileEncryptionKey = keyMap[it.key] ?: throw IllegalStateException("No key found")
            encryptionContextProvider.withEncryptionContextSuspendable(fileEncryptionKey) {
                val decodedMetadata = Base64.decodeBase64(it.value.metadata)
                val decryptedMetadata = decrypt(
                    content = EncryptedByteArray(decodedMetadata),
                    tag = EncryptionTag.FileData
                )
                val metadata = FileV1.FileMetadata.parseFrom(decryptedMetadata)
                val fileType = fileTypeDetector.getFileTypeFromMimeType(
                    MimeType(metadata.mimeType)
                )
                it.value.toDomain(
                    name = metadata.name,
                    mimeType = metadata.mimeType,
                    type = fileType.toDomain(),
                    shareId = shareId,
                    itemId = itemId
                )
            }
        }
        local.saveAttachmentsWithChunks(
            attachmentEntities = attachments.map { it.toEntity(userId) },
            chunkEntities = attachments.flatMap { attachment ->
                attachment.chunks.map {
                    it.toEntity(
                        attachmentId = attachment.id.id,
                        itemId = itemId.id,
                        shareId = shareId.id
                    )
                }
            }
        )
    }

    override suspend fun downloadAttachment(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        attachmentKey: AttachmentKey,
        chunks: List<Chunk>
    ): URI {
        if (chunks.isEmpty()) throw IllegalStateException("No chunks provided")
        val fileType = FileType.ItemAttachment(userId, shareId, itemId, attachmentId)
        val directory = fileUriGenerator.getDirectoryForFileType(fileType)
        val existingFileUri = withContext(appDispatchers.io) {
            val file = File(directory, attachmentId.id)
            if (file.exists() && file.length() != 0L) {
                fileUriGenerator.getFileProviderUri(file)
            } else null
        }
        if (existingFileUri != null) return existingFileUri
        val uri = fileUriGenerator.generate(fileType)
        val contentUri = Uri.parse(uri.toString())

        val encryptedItemKey = localItemDataSource.getById(shareId, itemId)?.encryptedKey
            ?: throw ItemKeyNotAvailableError()
        val itemKey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(encryptedItemKey))
        }
        val fileKey = encryptionContextProvider.withEncryptionContextSuspendable(itemKey) {
            val decodedFileKey = Base64.decodeBase64(attachmentKey.value)
            val decryptedKey =
                decrypt(EncryptedByteArray(decodedFileKey), EncryptionTag.FileKey)
            EncryptionKey(decryptedKey)
        }

        withContext(appDispatchers.io) {
            context.contentResolver.openOutputStream(contentUri)?.use { outputStream ->
                encryptionContextProvider.withEncryptionContextSuspendable(fileKey) {
                    chunks.sortedBy { it.index }.forEach { chunk ->
                        val encryptedChunk =
                            remote.downloadChunk(userId, shareId, itemId, attachmentId, chunk.id)
                        decrypt(encryptedChunk, EncryptionTag.FileData).let(outputStream::write)
                    }
                }
            } ?: throw IllegalStateException("Unable to open output stream for URI: $contentUri")
        }
        return URI.create(contentUri.toString())
    }

    companion object {
        private const val TAG = "AttachmentRepositoryImpl"
    }
}

fun Attachment.toEntity(userId: UserId): AttachmentEntity = AttachmentEntity(
    id = this.id.id,
    userId = userId.id,
    shareId = this.shareId.id,
    itemId = this.itemId.id,
    name = this.name,
    mimeType = this.mimeType,
    type = this.type.id,
    size = this.size,
    createTime = this.createTime,
    key = this.key.value,
    itemKeyRotation = this.itemKeyRotation
)

fun AttachmentEntity.toDomain(
    shareId: ShareId,
    itemId: ItemId,
    chunks: List<Chunk>
): Attachment = Attachment(
    id = AttachmentId(this.id),
    shareId = shareId,
    itemId = itemId,
    name = this.name,
    mimeType = this.mimeType,
    type = AttachmentType.entries.find { it.id == this.type } ?: AttachmentType.Unknown,
    size = this.size,
    createTime = this.createTime,
    key = AttachmentKey(this.key),
    itemKeyRotation = this.itemKeyRotation,
    chunks = chunks
)

fun Chunk.toEntity(
    attachmentId: String,
    itemId: String,
    shareId: String
): ChunkEntity = ChunkEntity(
    id = this.id.id,
    attachmentId = attachmentId,
    itemId = itemId,
    shareId = shareId,
    size = this.size,
    index = this.index
)

fun ChunkEntity.toDomain(): Chunk = Chunk(
    id = ChunkId(this.id),
    size = this.size,
    index = this.index
)
