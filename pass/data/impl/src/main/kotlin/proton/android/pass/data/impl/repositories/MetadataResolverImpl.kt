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

package proton.android.pass.data.impl.repositories

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonrust.api.FileType
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.MimeType
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.log.api.PassLogger
import java.net.URI
import javax.inject.Inject

class MetadataResolverImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers,
    private val fileTypeDetector: FileTypeDetector
) : MetadataResolver {

    override suspend fun extractMetadata(uri: URI): FileMetadata? {
        if (!isValidUri(uri)) {
            PassLogger.w(TAG, "Invalid URI: $uri")
            return null
        }

        val contentUri = Uri.parse(uri.toString())
        return withContext(appDispatchers.io) {
            runCatching {
                context.contentResolver.query(contentUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        extractMetadataFromCursor(cursor, contentUri)
                    } else {
                        PassLogger.w(TAG, "Cursor is empty for URI: $uri")
                        null
                    }
                }
            }.onFailure { e ->
                PassLogger.w(TAG, "Failed to extract metadata for URI: $uri")
                PassLogger.w(TAG, e)
            }.getOrNull()
        }
    }

    private suspend fun extractMetadataFromCursor(cursor: Cursor, contentUri: Uri): FileMetadata {
        val name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        val size = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
        val mimeType = detectMimeType(contentUri) ?: throw IllegalStateException("MIME type is null")
        val fileType = fileTypeDetector.getFileTypeFromMimeType(MimeType(mimeType))

        return FileMetadata(
            uri = URI.create(contentUri.toString()),
            name = name,
            size = size,
            mimeType = mimeType,
            attachmentType = fileType.toAttachmentType(),
            createTime = Clock.System.now() // Extract creation time from URI
        )
    }

    private fun FileType.toAttachmentType(): AttachmentType = when (this) {
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

    private suspend fun detectMimeType(contentUri: Uri): String? = withContext(appDispatchers.io) {
        runCatching {
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                val availableBytes = inputStream.available().coerceAtMost(200)
                if (availableBytes > 0) {
                    val buffer = ByteArray(availableBytes)
                    val bytesRead = inputStream.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        fileTypeDetector.getMimeTypeFromBytes(buffer.copyOf(bytesRead)).value
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }.onFailure { e ->
            PassLogger.w(TAG, "Failed to read bytes for MIME type detection")
            PassLogger.w(TAG, e)
        }.getOrNull()
    }

    private fun isValidUri(uri: URI): Boolean = runCatching {
        Uri.parse(uri.toString())
        true
    }.getOrElse { false }

    companion object {
        private const val TAG = "MetadataResolverImpl"
    }
}
