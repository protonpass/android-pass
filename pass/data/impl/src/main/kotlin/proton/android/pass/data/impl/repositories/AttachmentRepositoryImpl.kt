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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.MimeType
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.data.api.errors.ItemKeyNotAvailableError
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSource
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import java.net.URI
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers,
    private val metadataResolver: MetadataResolver,
    private val remote: RemoteAttachmentsDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val fileKeyRepository: FileKeyRepository,
    private val fileTypeDetector: FileTypeDetector,
    private val localItemDataSource: LocalItemDataSource
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
    ): Flow<List<Attachment>> = oneShot {
        val files = remote.retrieveAllFiles(userId, shareId, itemId).files
            .associateBy { it.fileId }
        val encryptedItemKey = localItemDataSource.getById(shareId, itemId)?.encryptedKey
            ?: throw ItemKeyNotAvailableError()
        val itemKey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(encryptedItemKey))
        }
        val keyMap = encryptionContextProvider.withEncryptionContextSuspendable(itemKey) {
            files.mapValues { file ->
                val decodedFileKey = Base64.decodeBase64(file.value.fileKey)
                val decryptedKey =
                    decrypt(EncryptedByteArray(decodedFileKey), EncryptionTag.FileKey)
                EncryptionKey(decryptedKey)
            }
        }
        files.map {
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
                it.value.toDomain(metadata.name, metadata.mimeType, fileType.toDomain())
            }
        }
    }
}
