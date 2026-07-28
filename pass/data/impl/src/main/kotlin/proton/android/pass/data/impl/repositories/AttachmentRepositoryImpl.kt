/*
 * Copyright (c) 2023-2026 Proton AG
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
import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toUri
import fileMetadata
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.files.api.FilesDirectories
import proton.android.pass.common.api.safeRunCatching
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
import proton.android.pass.data.api.repositories.PendingAttachmentLinkData
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.impl.crypto.attachment.AttachmentToReencrypt
import proton.android.pass.data.impl.crypto.attachment.DecryptFileAttachmentChunk
import proton.android.pass.data.impl.crypto.attachment.EncryptFileAttachmentChunk
import proton.android.pass.data.impl.crypto.attachment.EncryptFileAttachmentMetadata
import proton.android.pass.data.impl.crypto.attachment.ReencryptAttachment
import proton.android.pass.data.impl.crypto.attachment.ReencryptedKey
import proton.android.pass.data.impl.crypto.attachment.ReencryptedMetadata
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity
import proton.android.pass.data.impl.db.entities.attachments.AttachmentWithChunks
import proton.android.pass.data.impl.db.entities.attachments.ChunkEntity
import proton.android.pass.data.impl.extensions.readChunk
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.attachments.LocalAttachmentsDataSource
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSource
import proton.android.pass.data.impl.responses.attachments.ChunkApiModel
import proton.android.pass.data.impl.responses.attachments.FileApiModel
import proton.android.pass.data.impl.util.PaginatedResponse
import proton.android.pass.data.impl.util.fetchAllPaginated
import proton.android.pass.data.impl.util.isCompleteEncryptedFile
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.Chunk
import proton.android.pass.domain.attachments.ChunkId
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.domain.attachments.PendingAttachmentId
import proton.android.pass.domain.attachments.PersistentAttachmentId
import proton.android.pass.files.api.FileType
import proton.android.pass.files.api.FileUriGenerator
import proton.android.pass.log.api.PassLogger
import java.io.DataOutputStream
import java.io.File
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

fun interface LegacyAttachmentsDirProvider {
    fun getLegacyDir(): File
}

fun interface AttachmentsFilesDirProvider {
    fun getFilesDir(): File
}

@Suppress("LargeClass", "LongParameterList", "TooManyFunctions")
class AttachmentRepositoryImpl @Inject constructor(
    private val filesDirProvider: AttachmentsFilesDirProvider,
    private val contentResolver: ContentResolver,
    private val legacyAttachmentsDirProvider: LegacyAttachmentsDirProvider,
    private val appDispatchers: AppDispatchers,
    private val remote: RemoteAttachmentsDataSource,
    private val local: LocalAttachmentsDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val pendingAttachmentLinkRepository: PendingAttachmentLinkRepository,
    private val fileTypeDetector: FileTypeDetector,
    private val fileUriGenerator: FileUriGenerator,
    private val reencryptAttachment: ReencryptAttachment,
    private val userAccessDataRepository: UserAccessDataRepository,
    private val getItemKey: GetItemKey,
    private val encryptFileAttachmentMetadata: EncryptFileAttachmentMetadata,
    private val encryptFileAttachmentChunk: EncryptFileAttachmentChunk,
    private val decryptFileAttachmentChunk: DecryptFileAttachmentChunk
) : AttachmentRepository {

    private val legacyDirCleanupDone = AtomicBoolean(false)

    private val downloadMutexes = ConcurrentHashMap<String, Mutex>()

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
        val encryptedMetadata = encryptFileAttachmentMetadata.encrypt(fileMetadata)
        val chunks = ceil(metadata.size.toDouble() / CHUNK_SIZE).toInt()
        val pendingFileResponse = remote.createPendingFile(
            userId = userId,
            metadata = encryptedMetadata.encryptedMetadata,
            chunkCount = chunks,
            encryptionVersion = encryptedMetadata.encryptionVersion
        )
        val id = pendingFileResponse.file.fileID.let(::PendingAttachmentId)

        pendingAttachmentLinkRepository.addToLink(
            id,
            PendingAttachmentLinkData(
                linkKey = encryptedMetadata.fileKey,
                encryptionVersion = encryptedMetadata.encryptionVersion,
                numChunks = chunks
            )
        )
        return id
    }

    override suspend fun updatePendingAttachment(
        userId: UserId,
        attachmentId: PendingAttachmentId,
        metadata: FileMetadata
    ) {
        val linkData = pendingAttachmentLinkRepository.getToLinkData(attachmentId)
            ?: throw IllegalStateException("No PendingAttachmentLinkData found for attachment $attachmentId")

        val encryptedMetadata = encryptFileAttachmentMetadata.update(
            fileKey = linkData.linkKey,
            metadata = fileMetadata {
                this.name = metadata.name
                this.mimeType = metadata.mimeType
            },
            encryptionVersion = linkData.encryptionVersion
        )
        remote.updatePendingFile(
            userId = userId,
            pendingAttachmentId = attachmentId,
            metadata = encryptedMetadata.encryptedMetadata
        )
    }

    @Suppress("LongMethod")
    override suspend fun uploadPendingAttachment(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        uri: URI
    ) {
        val linkData = pendingAttachmentLinkRepository.getToLinkData(pendingAttachmentId)
            ?: throw IllegalStateException("No PendingAttachmentLinkData found for attachment $pendingAttachmentId")

        val contentUri: Uri = uri.toString().toUri()
        withContext(appDispatchers.io) {
            encryptionContextProvider.withEncryptionContextSuspendable(linkData.linkKey) {
                contentResolver.openInputStream(contentUri)?.use { inputStream ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var chunkIndex = 0
                    var bytesRead: Int

                    while (isActive) {
                        bytesRead = inputStream.readChunk(buffer)
                        if (bytesRead == 0) break

                        PassLogger.d(
                            TAG,
                            "Uploading chunk $chunkIndex / $${linkData.numChunks} for attachment $pendingAttachmentId"
                        )
                        remote.uploadPendingFile(
                            userId = userId,
                            pendingAttachmentId = pendingAttachmentId,
                            chunkIndex = chunkIndex,
                            encryptedByteArray = encryptFileAttachmentChunk(
                                encryptionContext = this@withEncryptionContextSuspendable,
                                chunkIndex = chunkIndex,
                                numChunks = linkData.numChunks,
                                chunk = buffer.copyOf(bytesRead),
                                encryptionVersion = linkData.encryptionVersion
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
        toLink: Map<PendingAttachmentId, PendingAttachmentLinkData>,
        toUnlink: Set<AttachmentId>
    ) {
        val encryptedItemKey = getItemKey(userId, shareId, itemId)
        val itemKey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(encryptedItemKey.key))
        }
        val toLinkEncrypted = encryptionContextProvider.withEncryptionContextSuspendable(itemKey) {
            toLink.mapValues { (_, linkData) ->
                val encryptedKey = encrypt(linkData.linkKey.value(), EncryptionTag.FileKey)
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
                val persistentIds = local.removeAttachmentsById(
                    userId = userId,
                    shareId = shareId,
                    itemId = itemId,
                    attachmentIdList = batchedToUnlink.getOrNull(i) ?: emptyList()
                )
                persistentIds.forEach { persistentId ->
                    val file = File(
                        filesDirProvider.getFilesDir(),
                        FilesDirectories.AttachmentsEnc.buildPath(
                            userId.id, shareId.id, itemId.id, persistentId
                        )
                    )
                    if (file.exists()) {
                        file.delete()
                    }
                }
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
            val encryptedMetadata = encryptFileAttachmentMetadata.update(
                fileKey = EncryptionKey(decryptedFileKey),
                metadata = updatedMetadata,
                encryptionVersion = attachment.encryptionVersion
            )
            val encodedMetadata = encryptedMetadata.encryptedMetadata

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
                attachments = listOf(
                    AttachmentToReencrypt(
                        encryptedMetadata = encodedMetadata,
                        encryptedKey = attachment.key,
                        encryptionVersion = attachment.encryptionVersion
                    )
                )
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
                    safeRunCatching { refreshActiveAttachments(userId, shareId, itemId) }
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
                    safeRunCatching { refreshAttachmentsForAllRevisions(userId, shareId, itemId) }
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

    private suspend fun saveRetrievedAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        fileDetails: List<FileApiModel>
    ) {
        val encryptedItemKey = getItemKey(userId, shareId, itemId)

        val attachmentsToReencrypt = fileDetails.map { fileDetail ->
            AttachmentToReencrypt(
                encryptedMetadata = fileDetail.metadata,
                encryptedKey = fileDetail.fileKey,
                encryptionVersion = fileDetail.encryptionVersion ?: DEFAULT_ENCRYPTION_VERSION

            )
        }
        val (reencryptedMetadatas, reencryptedKeys) = reencryptAttachment(
            encryptedItemKey = encryptedItemKey.key,
            attachments = attachmentsToReencrypt
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

    @Suppress("LongMethod")
    override suspend fun downloadAttachment(userId: UserId, attachment: Attachment): URI {
        if (attachment.chunks.isEmpty()) throw IllegalStateException("No chunks provided")

        if (!legacyDirCleanupDone.getAndSet(true)) {
            withContext(appDispatchers.io) {
                legacyAttachmentsDirProvider.getLegacyDir().deleteRecursively()
            }
        }

        val fileType = FileType.ItemAttachment(
            userId = userId,
            shareId = attachment.shareId,
            itemId = attachment.itemId,
            persistentId = attachment.persistentId
        )
        val directory = fileUriGenerator.getDirectoryForFileType(fileType)
        val encryptedCacheFile = File(directory, attachment.persistentId.id)

        downloadMutexFor(attachment.persistentId).withLock {
            if (!isCompleteEncryptedFile(encryptedCacheFile, attachment.chunks.size)) {
                downloadAndEncrypt(userId, attachment, encryptedCacheFile)
            }
        }
        val result = fileUriGenerator.getAttachmentPipeUri(
            userId = userId,
            shareId = attachment.shareId,
            itemId = attachment.itemId,
            persistentId = attachment.persistentId,
            mimeType = attachment.mimeType
        )
        updateDownloadStatus(userId, attachment, AttachmentDownloadStatus.Downloaded)
        return result
    }

    private fun downloadMutexFor(persistentId: PersistentAttachmentId): Mutex =
        downloadMutexes.getOrPut(persistentId.id) { Mutex() }

    private suspend fun downloadAndEncrypt(
        userId: UserId,
        attachment: Attachment,
        encryptedCacheFile: File
    ) {
        withContext(appDispatchers.io) {
            var success = false
            try {
                val fileKey = encryptionContextProvider.withEncryptionContextSuspendable {
                    EncryptionKey(decrypt(attachment.reencryptedKey))
                }
                val sortedChunks = attachment.chunks.sortedBy { it.index }
                encryptionContextProvider.withEncryptionContextSuspendable(fileKey) {
                    val fileKeyCtx = this@withEncryptionContextSuspendable
                    encryptionContextProvider.withEncryptionContextSuspendable {
                        val localCtx = this
                        DataOutputStream(encryptedCacheFile.outputStream().buffered()).use { dos ->
                            for (chunk in sortedChunks) {
                                currentCoroutineContext().ensureActive()
                                val remoteEncrypted = remote.downloadChunk(
                                    userId = userId,
                                    shareId = attachment.shareId,
                                    itemId = attachment.itemId,
                                    attachmentId = attachment.id,
                                    chunkId = chunk.id
                                )
                                val decrypted = decryptFileAttachmentChunk(
                                    encryptionContext = fileKeyCtx,
                                    chunk = remoteEncrypted,
                                    chunkIndex = chunk.index,
                                    numChunks = sortedChunks.size,
                                    encryptionVersion = attachment.encryptionVersion
                                )
                                val localEncrypted = localCtx.encrypt(decrypted)
                                dos.writeInt(localEncrypted.array.size)
                                dos.write(localEncrypted.array)
                            }
                        }
                    }
                }
                success = true
            } finally {
                if (!success) {
                    encryptedCacheFile.delete()
                }
            }
        }
    }

    override suspend fun updateDownloadStatus(
        userId: UserId,
        attachment: Attachment,
        status: AttachmentDownloadStatus
    ) {
        withContext(appDispatchers.io) {
            local.updateDownloadStatus(
                userId = userId,
                shareId = attachment.shareId,
                itemId = attachment.itemId,
                attachmentId = attachment.id,
                status = status
            )
        }
    }

    override suspend fun resetDownloadingToPending(userId: UserId, shareIds: List<ShareId>) {
        withContext(appDispatchers.io) {
            local.resetDownloadingToPending(userId, shareIds)
        }
    }

    override suspend fun resetIdleToPending(userId: UserId, shareIds: List<ShareId>) {
        withContext(appDispatchers.io) {
            local.resetIdleToPending(userId, shareIds)
        }
    }

    override suspend fun resetPausedToPending(userId: UserId, shareIds: List<ShareId>) {
        withContext(appDispatchers.io) {
            local.resetPausedToPending(userId, shareIds)
        }
    }

    override suspend fun clearDownloadedAttachments(userId: UserId, shareIds: List<ShareId>) {
        withContext(appDispatchers.io) {
            val baseDir = File(
                filesDirProvider.getFilesDir(),
                FilesDirectories.AttachmentsEnc.buildPath(userId.id)
            )
            shareIds.forEach { shareId ->
                File(baseDir, shareId.id).deleteRecursively()
            }
            local.resetDownloadedToIdle(userId, shareIds)
            local.resetDownloadingToIdle(userId, shareIds)
        }
    }

    override suspend fun clearAllDownloadedAttachments(userId: UserId) {
        withContext(appDispatchers.io) {
            val rootDir = File(
                filesDirProvider.getFilesDir(),
                FilesDirectories.AttachmentsEnc.buildPath(userId.id)
            )
            rootDir.deleteRecursively()
            local.resetAllDownloadingToIdle(userId)
            local.resetAllDownloadedToIdle(userId)
        }
    }

    override fun observeAllActiveAttachments(userId: UserId, shareIds: List<ShareId>): Flow<List<Attachment>> =
        local.observeAllActiveAttachments(userId, shareIds)
            .map { attachmentsWithChunks ->
                encryptionContextProvider.withEncryptionContextSuspendable {
                    attachmentsWithChunks.map { awc ->
                        val shareId = ShareId(awc.attachment.shareId)
                        val itemId = ItemId(awc.attachment.itemId)
                        awc.attachment.toDomain(
                            encryptionContext = this,
                            fileTypeDetector = fileTypeDetector,
                            shareId = shareId,
                            itemId = itemId,
                            chunks = awc.chunks.map(ChunkEntity::toDomain)
                        )
                    }
                }
            }

    override fun observePendingDownloads(
        userId: UserId,
        shareIds: List<ShareId>,
        includeFailed: Boolean
    ): Flow<List<Attachment>> = local.observePendingDownloads(userId, shareIds, includeFailed)
        .map { attachmentsWithChunks ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                attachmentsWithChunks.map { awc ->
                    val shareId = ShareId(awc.attachment.shareId)
                    val itemId = ItemId(awc.attachment.itemId)
                    awc.attachment.toDomain(
                        encryptionContext = this,
                        fileTypeDetector = fileTypeDetector,
                        shareId = shareId,
                        itemId = itemId,
                        chunks = awc.chunks.map(ChunkEntity::toDomain)
                    )
                }
            }
        }

    override fun observeDownloadProgress(userId: UserId, shareIds: List<ShareId>): Flow<Pair<Int, Int>> = combine(
        local.observeDownloadedCount(userId, shareIds),
        local.observeTotalCount(userId, shareIds)
    ) { downloaded, total ->
        downloaded to total
    }

    override fun observeAttachmentById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Flow<Attachment?> = local.observeAttachmentById(userId, shareId, itemId, attachmentId)
        .map { awc ->
            awc?.let {
                encryptionContextProvider.withEncryptionContextSuspendable {
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

    override suspend fun refreshAttachmentsForItems(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>
    ): List<Pair<ShareId, ItemId>> {
        val failed = mutableListOf<Pair<ShareId, ItemId>>()
        items.forEach { (shareId, itemId) ->
            safeRunCatching { refreshActiveAttachments(userId, shareId, itemId) }
                .onFailure { error ->
                    PassLogger.w(TAG, "Failed to refresh attachments for $shareId / $itemId")
                    PassLogger.w(TAG, error)
                    failed += shareId to itemId
                }
        }
        return failed
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
    reencryptedMetadata = reencryptedMetadata,
    encryptionVersion = this.encryptionVersion ?: DEFAULT_ENCRYPTION_VERSION,
    downloadStatus = AttachmentDownloadStatus.Idle
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
        chunks = chunks,
        encryptionVersion = this.encryptionVersion,
        downloadStatus = this.downloadStatus
    )
}

fun ChunkEntity.toDomain(): Chunk = Chunk(
    id = ChunkId(this.id),
    size = this.size,
    index = this.index
)

private const val DEFAULT_ENCRYPTION_VERSION = 1
