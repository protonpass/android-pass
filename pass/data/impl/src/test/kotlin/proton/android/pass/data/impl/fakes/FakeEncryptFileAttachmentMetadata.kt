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

package proton.android.pass.data.impl.fakes

import proton.android.pass.commonrust.fakes.FakeItemContentSerializer
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.data.impl.crypto.attachment.EncryptFileAttachmentMetadata
import proton.android.pass.data.impl.crypto.attachment.EncryptedFileAttachmentMetadata
import proton.android.pass.domain.attachments.AttachmentContent

class FakeEncryptFileAttachmentMetadata : EncryptFileAttachmentMetadata {

    private val itemContentSerializer = FakeItemContentSerializer()

    override suspend fun encrypt(metadata: AttachmentContent): EncryptedFileAttachmentMetadata =
        EncryptedFileAttachmentMetadata(
            encryptedMetadata = "",
            fileKey = EncryptionKey(ByteArray(32)),
            encryptionVersion = 1
        )

    override suspend fun update(
        fileKey: EncryptionKey,
        metadata: AttachmentContent,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata = EncryptedFileAttachmentMetadata(
        encryptedMetadata = "",
        fileKey = fileKey,
        encryptionVersion = encryptionVersion
    )

    override suspend fun rename(
        fileKey: EncryptionKey,
        decryptedMetadata: ByteArray,
        name: String,
        encryptionVersion: Int
    ): EncryptedFileAttachmentMetadata = EncryptedFileAttachmentMetadata(
        encryptedMetadata = "",
        fileKey = fileKey,
        encryptionVersion = encryptionVersion
    )

    override fun deserialize(decryptedMetadata: ByteArray): AttachmentContent =
        itemContentSerializer.deserializeAttachmentContent(decryptedMetadata)
}
