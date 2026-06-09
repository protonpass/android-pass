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

package proton.android.pass.data.impl.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.impl.responses.PopularServiceResponse
import proton.android.pass.log.api.PassLogger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPopularServicesDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers
) : LocalPopularServicesDataSource {

    override suspend fun store(services: List<PopularServiceResponse>): Result<Unit> = withContext(appDispatchers.io) {
        safeRunCatching {
            val file = cacheFile()
            val tmp = File(file.parentFile, "$FILE_NAME.tmp")
            try {
                tmp.writeText(json.encodeToString(services))
                if (!tmp.renameTo(file)) {
                    tmp.copyTo(file, overwrite = true)
                }
            } finally {
                tmp.delete()
            }
        }.onFailure {
            PassLogger.w(TAG, "Failed to store popular services")
            PassLogger.w(TAG, it)
        }
    }

    override suspend fun read(): List<PopularServiceResponse> = withContext(appDispatchers.io) {
        val file = cacheFile()
        if (!file.exists()) return@withContext emptyList()
        safeRunCatching {
            json.decodeFromString<List<PopularServiceResponse>>(file.readText())
        }.getOrElse {
            PassLogger.w(TAG, "Failed to read popular services")
            PassLogger.w(TAG, it)
            emptyList()
        }
    }

    private fun cacheFile(): File {
        val dir = File(context.cacheDir, DIR_NAME).apply { if (!exists()) mkdirs() }
        return File(dir, FILE_NAME)
    }

    private companion object {
        private const val DIR_NAME = "popular_services"
        private const val FILE_NAME = "services.json"
        private const val TAG = "LocalPopularServicesDataSourceImpl"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
