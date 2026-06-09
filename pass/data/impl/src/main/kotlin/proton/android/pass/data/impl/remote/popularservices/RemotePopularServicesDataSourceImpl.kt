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

package proton.android.pass.data.impl.remote.popularservices

import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.api.PublicOkhttpClient
import proton.android.pass.data.api.errors.ResponseSizeExceededError
import proton.android.pass.data.impl.responses.PopularServiceResponse
import java.io.IOException
import javax.inject.Inject

class RemotePopularServicesDataSourceImpl @Inject constructor(
    @param:PublicOkhttpClient private val okHttpClient: OkHttpClient,
    private val appDispatchers: AppDispatchers
) : RemotePopularServicesDataSource {

    override suspend fun fetch(): List<PopularServiceResponse> = withContext(appDispatchers.io) {
        val request = Request.Builder().url(POPULAR_SERVICES_URL).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code $response")
            }

            val contentLength = response.body?.contentLength() ?: -1
            if (contentLength > MAX_RESPONSE_SIZE_BYTES) {
                throw ResponseSizeExceededError(
                    url = POPULAR_SERVICES_URL,
                    contentLength = contentLength,
                    maxSize = MAX_RESPONSE_SIZE_BYTES
                )
            }

            val source = response.body?.source()
                ?: throw IOException("Empty response body from $POPULAR_SERVICES_URL")
            if (source.request(MAX_RESPONSE_SIZE_BYTES + 1)) {
                throw ResponseSizeExceededError(
                    url = POPULAR_SERVICES_URL,
                    contentLength = contentLength,
                    maxSize = MAX_RESPONSE_SIZE_BYTES
                )
            }

            val body = source.buffer.readUtf8()

            json.decodeFromString<List<PopularServiceResponse>>(body)
        }
    }

    private companion object {
        private const val MAX_RESPONSE_SIZE_BYTES = 2 * 1024 * 1024L

        private val json = Json { ignoreUnknownKeys = true }
    }
}

private const val POPULAR_SERVICES_URL =
    "https://proton.me/download/pass/popular-services/services.json"
