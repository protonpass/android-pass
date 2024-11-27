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

package proton.android.pass.commonrust.impl

import proton.android.pass.commonrust.FileDecoder
import proton.android.pass.commonrust.FileGroup
import proton.android.pass.commonrust.api.FileType
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.MimeType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTypeDetectorImpl @Inject constructor() : FileTypeDetector {

    private val fileDecoder by lazy { FileDecoder() }

    override fun getMimeTypeFromBytes(bytes: ByteArray): MimeType =
        fileDecoder.getMimetypeFromContent(bytes).let(::MimeType)

    override fun getFileTypeFromMimeType(mimeType: MimeType): FileType =
        fileDecoder.getFilegroupFromMimetype(mimeType.value)
            .let {
                when (it) {
                    FileGroup.IMAGE -> FileType.RasterImage
                    FileGroup.PHOTO -> FileType.Photo
                    FileGroup.VECTOR_IMAGE -> FileType.VectorImage
                    FileGroup.VIDEO -> FileType.Video
                    FileGroup.AUDIO -> FileType.Audio
                    FileGroup.KEY -> FileType.Key
                    FileGroup.TEXT -> FileType.Text
                    FileGroup.CALENDAR -> FileType.Calendar
                    FileGroup.PDF -> FileType.Pdf
                    FileGroup.WORD -> FileType.Word
                    FileGroup.POWER_POINT -> FileType.PowerPoint
                    FileGroup.EXCEL -> FileType.Excel
                    FileGroup.DOCUMENT -> FileType.Document
                    FileGroup.UNKNOWN -> FileType.Unknown
                }
            }
}
