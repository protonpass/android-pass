/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.log.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.log.api.LogFileManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogFileManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers
) : LogFileManager {

    private val logsDirectory: File
        get() = File(context.cacheDir, LOGS_DIR_NAME)

    override suspend fun getLogFile(userId: UserId?): File = withContext(appDispatchers.io) {
        val fileName = if (userId != null) {
            "user_${userId.id.take(4)}_${userId.id.takeLast(4)}.log"
        } else {
            NOT_AUTHENTICATED_FILE_NAME
        }
        File(logsDirectory, fileName)
    }

    override suspend fun initializeLogDirectory() = withContext<Unit>(appDispatchers.io) {
        safeRunCatching {
            if (!logsDirectory.exists()) {
                logsDirectory.mkdirs()
            } else {
                logsDirectory.listFiles { _, name -> name.endsWith(".tmp") }?.forEach { tempFile ->
                    tempFile.delete()
                }
            }
        }
    }

    override suspend fun ensureLogFileExists(file: File) = withContext<Unit>(appDispatchers.io) {
        safeRunCatching {
            file.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
            if (!file.exists()) {
                file.createNewFile()
            }
        }
    }

    override suspend fun getAllUserLogFiles(): List<File> = withContext(appDispatchers.io) {
        safeRunCatching {
            logsDirectory.listFiles()?.filter { it.extension == "log" } ?: emptyList()
        }.getOrElse { emptyList() }
    }

    override suspend fun deleteLogFile(file: File) = withContext<Unit>(appDispatchers.io) {
        safeRunCatching {
            if (file.exists()) {
                file.delete()
            }
        }
    }

    companion object {
        private const val TAG = "LogFileManagerImpl"
        private const val LOGS_DIR_NAME = "logs"
        private const val NOT_AUTHENTICATED_FILE_NAME = "not_authenticated.log"
    }
}
