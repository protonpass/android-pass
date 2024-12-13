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
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.files.api.CacheCleaner
import proton.android.pass.files.api.DirectoryType
import proton.android.pass.files.impl.CacheDirectories.Camera
import proton.android.pass.log.api.PassLogger
import java.io.File
import javax.inject.Inject

class CacheCleanerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers
) : CacheCleaner {
    override suspend fun deleteDir(type: DirectoryType) {
        withContext(appDispatchers.io) {
            when (type) {
                DirectoryType.CameraTemp -> {
                    runCatching {
                        val file = File(context.cacheDir, Camera.value)
                        file.deleteRecursively()
                    }.onFailure {
                        PassLogger.w(TAG, "Failed to delete cache directory")
                        PassLogger.w(TAG, it)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "CacheCleanerImpl"
    }
}
