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

package proton.android.pass.files.impl

import android.content.Context
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.files.api.CacheDirectories
import proton.android.pass.files.api.FileType
import proton.android.pass.files.api.FileUriGenerator
import proton.android.pass.files.api.FilesDirectories
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class FileUriGeneratorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers
) : FileUriGenerator {

    private val cameraFileCounter = AtomicInteger(0)

    override suspend fun generate(fileType: FileType): URI {
        val file = createFileForType(fileType)
        return getFileProviderUri(file)
    }

    override fun getFileProviderUri(file: File): URI {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        return URI.create(uri.toString())
    }

    private suspend fun createFileForType(fileType: FileType): File = withContext(appDispatchers.io) {
        val directory = getDirectoryForFileType(fileType)
        when (fileType) {
            FileType.CameraCache -> createUniqueFile(directory, CAMERA_PREFIX, CAMERA_SUFFIX)
            is FileType.ItemAttachment -> File(directory, fileType.persistentId.id).apply {
                if (!exists()) createNewFile()
            }
        }
    }

    override suspend fun getDirectoryForFileType(fileType: FileType): File = withContext(appDispatchers.io) {
        when (fileType) {
            FileType.CameraCache -> File(
                context.cacheDir,
                CacheDirectories.Camera.value
            ).apply { ensureDirectoryExists(this) }

            is FileType.ItemAttachment -> File(
                context.filesDir,
                FilesDirectories.Attachments.value +
                    SpecialCharacters.SLASH +
                    fileType.userId.id +
                    SpecialCharacters.SLASH +
                    fileType.shareId.id +
                    SpecialCharacters.SLASH +
                    fileType.itemId.id
            ).apply { ensureDirectoryExists(this) }
        }
    }

    private suspend fun createUniqueFile(
        directory: File,
        prefix: String,
        suffix: String
    ): File = withContext(appDispatchers.io) {
        var file: File
        do {
            val name = "$prefix${cameraFileCounter.getAndIncrement()}$suffix"
            file = File(directory, name)
        } while (file.exists())
        file.createNewFile()
        file
    }

    private suspend fun ensureDirectoryExists(directory: File) = withContext(appDispatchers.io) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Failed to create directory: ${directory.absolutePath}")
        }
    }

    private companion object {
        const val CAMERA_PREFIX = "photo_"
        const val CAMERA_SUFFIX = ".jpg"
    }
}
