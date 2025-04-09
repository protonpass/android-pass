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

interface EncryptFileAttachmentChunk {
    operator fun invoke(
        encryptionContext: EncryptionContext,
        chunkIndex: Int,
        numChunks: Int,
        chunk: ByteArray,
        encryptionVersion: Int
    ): EncryptedByteArray
}

class EncryptFileAttachmentChunkImpl @Inject constructor() : EncryptFileAttachmentChunk {
    override fun invoke(
        encryptionContext: EncryptionContext,
        chunkIndex: Int,
        numChunks: Int,
        chunk: ByteArray,
        encryptionVersion: Int
    ): EncryptedByteArray = when (encryptionVersion) {
        1 -> encryptV1(encryptionContext = encryptionContext, chunk = chunk)
        2 -> encryptV2(
            encryptionContext = encryptionContext,
            chunkIndex = chunkIndex,
            numChunks = numChunks,
            chunk = chunk
        )
        else -> throw IllegalStateException("Unknown encryption version $encryptionVersion")
    }

    private fun encryptV1(encryptionContext: EncryptionContext, chunk: ByteArray) =
        encryptionContext.encrypt(chunk, EncryptionTag.FileData)

    private fun encryptV2(
        encryptionContext: EncryptionContext,
        chunkIndex: Int,
        numChunks: Int,
        chunk: ByteArray
    ) = encryptionContext.encrypt(
        content = chunk,
        tag = EncryptionTag.FileDataV2(chunkIndex = chunkIndex, numChunks = numChunks)
    )
}
