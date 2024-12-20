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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.api.usecases.CheckIfItemExists
import proton.android.pass.data.api.usecases.CheckIfShareExists
import proton.android.pass.data.api.usecases.CheckIfUserExists
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.files.api.DirectoryCleaner
import proton.android.pass.files.api.DirectoryType
import proton.android.pass.files.impl.CacheDirectories.Camera
import proton.android.pass.files.impl.FilesDirectories.Attachments
import proton.android.pass.log.api.PassLogger
import java.io.File
import javax.inject.Inject

class DirectoryCleanerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers,
    private val checkIfUserExists: CheckIfUserExists,
    private val checkIfShareExists: CheckIfShareExists,
    private val checkIfItemExists: CheckIfItemExists
) : DirectoryCleaner {
    override suspend fun deleteDir(type: DirectoryType) {
        withContext(appDispatchers.io) {
            when (type) {
                DirectoryType.CameraTemp ->
                    runCatching {
                        val file = File(context.cacheDir, Camera.value)
                        file.deleteRecursively()
                    }.onFailure {
                        PassLogger.w(TAG, "Failed to delete cache directory")
                        PassLogger.w(TAG, it)
                    }

                DirectoryType.OrphanedAttachments ->
                    runCatching {
                        val attachmentsDirectory = File(context.filesDir, Attachments.value)
                        processUserDirs(attachmentsDirectory)
                            .forEach { userDir ->
                                processShareDirs(userDir)
                                    .forEach { shareDir ->
                                        processItemDirs(shareDir)
                                    }
                            }
                    }.onFailure {
                        PassLogger.w(TAG, "Failed to delete orphaned attachments")
                        PassLogger.w(TAG, it)
                    }
            }
        }
    }

    private suspend fun DirectoryCleanerImpl.processUserDirs(attachmentsDirectory: File) =
        processDirectory(attachmentsDirectory) { userDir ->
            val userExists = checkIfUserExists(UserId(userDir.name))
            if (!userExists) {
                userDir.deleteRecursively()
                PassLogger.d(
                    TAG,
                    "Deleted attachment user directory: ${userDir.path}"
                )
                null
            } else {
                userDir
            }
        }

    private suspend fun DirectoryCleanerImpl.processShareDirs(userDir: File) = processDirectory(userDir) { shareDir ->
        val shareExists = checkIfShareExists(ShareId(shareDir.name))
        if (!shareExists) {
            shareDir.deleteRecursively()
            PassLogger.d(TAG, "Deleted attachment share directory: ${shareDir.path}")
            null
        } else {
            shareDir
        }
    }

    private suspend fun DirectoryCleanerImpl.processItemDirs(shareDir: File) {
        processDirectory(shareDir) { itemDir ->
            val itemExists = checkIfItemExists(ShareId(shareDir.name), ItemId(itemDir.name))
            if (!itemExists) {
                itemDir.deleteRecursively()
                PassLogger.d(TAG, "Deleted attachment item directory: ${itemDir.path}")
                null
            } else {
                itemDir
            }
        }
    }

    private suspend fun processDirectory(directory: File, filter: suspend (File) -> File?): List<File> =
        directory.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { subDir ->
                if (deleteIfEmpty(subDir)) null else filter(subDir)
            }
            ?: emptyList()

    private fun deleteIfEmpty(file: File): Boolean = file.listFiles()?.isEmpty() == true && file.delete().also {
        if (it) {
            PassLogger.d(TAG, "Deleted empty directory: ${file.path}")
        }
    }

    companion object {
        private const val TAG = "DirectoryCleanerImpl"
    }
}
