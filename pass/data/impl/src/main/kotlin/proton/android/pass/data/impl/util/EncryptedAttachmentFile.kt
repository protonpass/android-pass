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

package proton.android.pass.data.impl.util

import java.io.File
import java.io.RandomAccessFile

internal const val MAX_ENCRYPTED_CHUNK_SIZE = 10 * 1024 * 1024 + 64

private const val CHUNK_LENGTH_PREFIX_BYTES = 4

internal fun isCompleteEncryptedFile(file: File, expectedChunkCount: Int): Boolean {
    if (!file.isFile) return false
    val fileLength = file.length()
    if (fileLength == 0L) return false
    return runCatching {
        RandomAccessFile(file, "r").use { raf ->
            var offset = 0L
            var chunkCount = 0
            while (offset < fileLength) {
                if (fileLength - offset < CHUNK_LENGTH_PREFIX_BYTES) return@runCatching false
                raf.seek(offset)
                val len = raf.readInt()
                if (len !in 1..MAX_ENCRYPTED_CHUNK_SIZE) return@runCatching false
                offset += CHUNK_LENGTH_PREFIX_BYTES + len
                if (offset > fileLength) return@runCatching false
                chunkCount++
            }
            chunkCount == expectedChunkCount
        }
    }.getOrDefault(false)
}
