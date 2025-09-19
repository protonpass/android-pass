/*
 * Copyright (c) 2025 Proton AG
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

import FileV1
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

data class EncryptedFileAttachmentMetadata(
    val encryptedMetadata: String,
    val encryptionVersion: Int,
    val fileKey: EncryptionKey
)

interface EncryptFileAttachmentMetadata {
    suspend fun encrypt(metadata: FileV1.FileMetadata): EncryptedFileAttachmentMetadata
    suspend fun update(
        fileKey: EncryptionKey,
        metadata: FileV1.FileMetadata,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata
}

class EncryptFileAttachmentMetadataImpl @Inject constructor(
    private val ffRepo: FeatureFlagsPreferencesRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : EncryptFileAttachmentMetadata {

    override suspend fun encrypt(metadata: FileV1.FileMetadata): EncryptedFileAttachmentMetadata {
        val fileKey = EncryptionKey.generate()
        return encrypt(metadata, fileKey, 2)
    }

    override suspend fun update(
        fileKey: EncryptionKey,
        metadata: FileV1.FileMetadata,
        encryptionVersion: Int
    ) = encrypt(metadata, fileKey, encryptionVersion)

    private suspend fun encrypt(
        metadata: FileV1.FileMetadata,
        key: EncryptionKey,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata {
        return when (encryptionVersion) {
            1 -> encryptV1(metadata, key)
            2 -> encryptV2(metadata, key)
            else -> throw IllegalStateException("Unknown encryptionVersion $encryptionVersion")
        }
    }


    private suspend fun encryptV1(
        fileMetadata: FileV1.FileMetadata,
        fileKey: EncryptionKey
    ): EncryptedFileAttachmentMetadata {
        val encryptedMetadata =
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey.clone()) {
                encrypt(fileMetadata.toByteArray(), EncryptionTag.FileData)
            }

        return EncryptedFileAttachmentMetadata(
            encryptedMetadata = Base64.encodeBase64String(encryptedMetadata.array),
            encryptionVersion = 1,
            fileKey = fileKey
        )
    }

    private suspend fun encryptV2(
        fileMetadata: FileV1.FileMetadata,
        fileKey: EncryptionKey
    ): EncryptedFileAttachmentMetadata {
        val encryptedMetadata =
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey.clone()) {
                encrypt(fileMetadata.toByteArray(), EncryptionTag.FileMetadata)
            }

        return EncryptedFileAttachmentMetadata(
            encryptedMetadata = Base64.encodeBase64String(encryptedMetadata.array),
            encryptionVersion = 2,
            fileKey = fileKey
        )
    }

}
