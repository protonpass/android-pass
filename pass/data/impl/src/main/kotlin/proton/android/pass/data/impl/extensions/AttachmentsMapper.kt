/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.extensions

import proton.android.pass.commonrust.api.FileType
import proton.android.pass.data.impl.responses.attachments.ChunkResponse
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.Chunk
import proton.android.pass.domain.attachments.ChunkId

fun FileType.toDomain(): AttachmentType = when (this) {
    FileType.RasterImage -> AttachmentType.RasterImage
    FileType.VectorImage -> AttachmentType.VectorImage
    FileType.Photo -> AttachmentType.Photo
    FileType.Video -> AttachmentType.Video
    FileType.Audio -> AttachmentType.Audio
    FileType.Key -> AttachmentType.Key
    FileType.Text -> AttachmentType.Text
    FileType.Calendar -> AttachmentType.Calendar
    FileType.Pdf -> AttachmentType.Pdf
    FileType.Word -> AttachmentType.Word
    FileType.PowerPoint -> AttachmentType.PowerPoint
    FileType.Excel -> AttachmentType.Excel
    FileType.Document -> AttachmentType.Document
    FileType.Unknown -> AttachmentType.Unknown
}

fun ChunkResponse.toDomain(): Chunk = Chunk(
    id = ChunkId(chunkId),
    index = index,
    size = size
)
