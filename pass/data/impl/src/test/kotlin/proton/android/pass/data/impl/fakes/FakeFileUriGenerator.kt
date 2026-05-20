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

import java.io.File
import java.net.URI
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.PersistentAttachmentId
import proton.android.pass.files.api.FileType
import proton.android.pass.files.api.FileUriGenerator
import proton.android.pass.files.api.FilesDirectories

class FakeFileUriGenerator(
    private val filesDir: File,
    private val cacheDir: File
) : FileUriGenerator {

    override suspend fun generate(fileType: FileType): URI {
        require(fileType is FileType.ItemAttachment) { "generate() only supports ItemAttachment" }
        val directory = getDirectoryForFileType(fileType)
        val file = File(directory, fileType.persistentId.id)
        file.createNewFile()
        return URI.create(file.toURI().toString())
    }

    override suspend fun getDirectoryForFileType(fileType: FileType): File {
        return when (fileType) {
            is FileType.ItemAttachment -> {
                File(
                    filesDir,
                    FilesDirectories.AttachmentsEnc.value +
                        "/${fileType.userId.id}/${fileType.shareId.id}/${fileType.itemId.id}"
                )
                    .apply { mkdirs() }
            }

            FileType.CameraCache -> {
                File(filesDir, "camera").apply { mkdirs() }
            }
        }
    }

    override fun getFileProviderUri(file: File): URI = URI.create(file.toURI().toString())

    override suspend fun getShareTempDirectory(): File = File(cacheDir, "share").apply { mkdirs() }

    override fun getAttachmentPipeUri(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        persistentId: PersistentAttachmentId,
        mimeType: String
    ): URI = URI.create(
        "content://test.attachmentprovider/attachment/${userId.id}/${shareId.id}/${itemId.id}/${persistentId.id}"
    )

    fun encryptedCacheFileFor(userId: UserId, attachment: Attachment): File = File(
        File(
            filesDir,
            FilesDirectories.AttachmentsEnc.value +
                "/${userId.id}/${attachment.shareId.id}/${attachment.itemId.id}"
        ),
        attachment.persistentId.id
    )
}
