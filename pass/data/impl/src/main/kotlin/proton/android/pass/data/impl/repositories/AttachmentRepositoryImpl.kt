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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.MimeType
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.data.api.errors.AddressIdNotAvailableError
import proton.android.pass.data.api.errors.ItemKeyNotAvailableError
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.impl.extensions.toDomain
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
    private val remote: RemoteAttachmentsDataSource,
    private val fileTypeDetector: FileTypeDetector,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val fileKeyRepository: FileKeyRepository,
    private val userAddressRepository: UserAddressRepository,
    private val itemKeyRepository: ItemKeyRepository
) : AttachmentRepository {

    override suspend fun createPendingAttachment(
        userId: UserId,
        name: String,
        mimeType: String
    ): AttachmentId {
        val fileKey = EncryptionKey.generate()
        val metadata = fileMetadata {
            this.name = name
            this.mimeType = mimeType
        }
        val encryptedMetadata =
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey.clone()) {
                encrypt(metadata.toByteArray())
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
        if (fileKey.isEmpty()) {
            throw IllegalStateException("Key has been cleared")
        }
        val contentUri: Uri = Uri.parse(uri.toString())
        val encryptedByteArray = withContext(appDispatchers.io) {
            context.contentResolver.openInputStream(contentUri)?.use { inputSteam ->
                encryptionContextProvider.withEncryptionContextSuspendable(fileKey.clone()) {
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
        val addressId = userAddressRepository.getAddresses(userId).primary()?.addressId
            ?: throw AddressIdNotAvailableError()
        val (_, itemKey) = itemKeyRepository.getLatestItemKey(
            userId = userId,
            addressId = addressId,
            itemId = itemId,
            shareId = shareId
        ).firstOrNull() // get from item entity
            ?: throw ItemKeyNotAvailableError()
        val decryptedItemKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(itemKey.key))
        }
        val mappings: Map<AttachmentId, EncryptionKey> = fileKeyRepository.getAllMappings()
        val encryptedContents: Map<AttachmentId, EncryptedString> =
            encryptionContextProvider.withEncryptionContext(decryptedItemKey.clone()) {
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
        return@oneShot encryptionContextProvider.withEncryptionContextSuspendable {
            files.map {
                val decodedMetadata = Base64.decodeBase64(it.metadata)
                val decryptedMetadata =
                    decrypt(EncryptedByteArray(decodedMetadata), EncryptionTag.FileKey)
                val metadata = FileV1.File.parseFrom(decryptedMetadata).metadata
                val fileType = fileTypeDetector.getFileTypeFromMimeType(
                    MimeType(metadata.mimeType)
                )
                it.toDomain(metadata.name, metadata.mimeType, fileType.toDomain())
            }
        }
    }
}
