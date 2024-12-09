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
import proton.android.pass.files.api.FileType
import proton.android.pass.files.api.FileUriGenerator
import java.io.File
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class FileUriGeneratorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers
) : FileUriGenerator {

    private val cameraFileCounter = AtomicInteger(0)

    override suspend fun generate(fileType: FileType): URI = withContext(appDispatchers.io) {
        val file = createFileForType(fileType)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        URI.create(uri.toString())
    }

    private fun createFileForType(fileType: FileType): File {
        val (directoryName, prefix, suffix) = when (fileType) {
            FileType.CameraTemp -> Triple("camera", "photo_", ".jpg")
        }

        val cacheDir = File(context.cacheDir, directoryName).apply {
            if (!exists()) mkdirs()
        }

        return when (fileType) {
            FileType.CameraTemp -> {
                var file: File
                do {
                    val name = "$prefix${cameraFileCounter.getAndIncrement()}$suffix"
                    file = File(cacheDir, name)
                } while (file.exists())

                file.apply { createNewFile() }
            }
        }
    }
}
