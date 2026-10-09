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

package proton.android.pass.data.impl.crypto.attachment

import proton.android.pass.commonrust.api.ItemContentSerializer
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.domain.attachments.AttachmentContent
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

data class EncryptedFileAttachmentMetadata(
    val encryptedMetadata: String,
    val encryptionVersion: Int,
    val fileKey: EncryptionKey
)

interface EncryptFileAttachmentMetadata {
    suspend fun encrypt(metadata: AttachmentContent): EncryptedFileAttachmentMetadata
    suspend fun update(
        fileKey: EncryptionKey,
        metadata: AttachmentContent,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata

    suspend fun rename(
        fileKey: EncryptionKey,
        decryptedMetadata: ByteArray,
        name: String,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata

    fun deserialize(decryptedMetadata: ByteArray): AttachmentContent
}

class EncryptFileAttachmentMetadataImpl @Inject constructor(
    private val ffRepo: FeatureFlagsPreferencesRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val itemContentSerializer: ItemContentSerializer
) : EncryptFileAttachmentMetadata {

    override suspend fun encrypt(metadata: AttachmentContent): EncryptedFileAttachmentMetadata {
        val fileKey = EncryptionKey.generate()
        return encrypt(metadata, fileKey, 2)
    }

    override suspend fun update(
        fileKey: EncryptionKey,
        metadata: AttachmentContent,
        encryptionVersion: Int
    ) = encrypt(metadata, fileKey, encryptionVersion)

    override fun deserialize(decryptedMetadata: ByteArray): AttachmentContent =
        itemContentSerializer.deserializeAttachmentContent(decryptedMetadata)

    override suspend fun rename(
        fileKey: EncryptionKey,
        decryptedMetadata: ByteArray,
        name: String,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata = encryptBytes(
        serialized = itemContentSerializer.updateAttachmentName(decryptedMetadata, name),
        key = fileKey,
        encryptionVersion = encryptionVersion
    )

    private suspend fun encrypt(
        metadata: AttachmentContent,
        key: EncryptionKey,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata = encryptBytes(
        serialized = itemContentSerializer.serializeAttachmentContent(metadata),
        key = key,
        encryptionVersion = encryptionVersion
    )

    private suspend fun encryptBytes(
        serialized: ByteArray,
        key: EncryptionKey,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata = when (encryptionVersion) {
        1 -> encryptWithTag(serialized, key, EncryptionTag.FileData, 1)
        2 -> encryptWithTag(serialized, key, EncryptionTag.FileMetadata, 2)
        else -> throw IllegalStateException("Unknown encryptionVersion $encryptionVersion")
    }

    private suspend fun encryptWithTag(
        serialized: ByteArray,
        fileKey: EncryptionKey,
        tag: EncryptionTag,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata {
        val encryptedMetadata =
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey.clone()) {
                encrypt(serialized, tag)
            }

        return EncryptedFileAttachmentMetadata(
            encryptedMetadata = Base64.encodeBase64String(encryptedMetadata.array),
            encryptionVersion = encryptionVersion,
            fileKey = fileKey
        )
    }
}
