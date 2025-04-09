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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionTag
import javax.inject.Inject

interface DecryptFileAttachmentChunk {
    operator fun invoke(
        encryptionContext: EncryptionContext,
        chunk: EncryptedByteArray,
        chunkIndex: Int,
        numChunks: Int,
        encryptionVersion: Int
    ): ByteArray
}

class DecryptFileAttachmentChunkImpl @Inject constructor() : DecryptFileAttachmentChunk {
    override fun invoke(
        encryptionContext: EncryptionContext,
        chunk: EncryptedByteArray,
        chunkIndex: Int,
        numChunks: Int,
        encryptionVersion: Int
    ) = when (encryptionVersion) {
        1 -> decryptV1(encryptionContext, chunk)
        2 -> decryptV2(encryptionContext, chunk, chunkIndex, numChunks)
        else -> throw IllegalStateException("Unknown encryptionVersion: $encryptionVersion")
    }

    private fun decryptV1(encryptionContext: EncryptionContext, chunk: EncryptedByteArray) =
        encryptionContext.decrypt(chunk, EncryptionTag.FileData)

    private fun decryptV2(
        encryptionContext: EncryptionContext,
        chunk: EncryptedByteArray,
        chunkIndex: Int,
        numChunks: Int
    ) = encryptionContext.decrypt(
        content = chunk,
        tag = EncryptionTag.FileDataV2(chunkIndex = chunkIndex, numChunks = numChunks)
    )
}
