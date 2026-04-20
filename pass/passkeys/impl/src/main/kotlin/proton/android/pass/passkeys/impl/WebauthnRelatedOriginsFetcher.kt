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

package proton.android.pass.passkeys.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import proton.android.pass.commonrust.MobileFetchException
import proton.android.pass.commonrust.MobileWebauthnClientFetcher
import proton.android.pass.commonrust.MobileWebauthnDomainsResponse
import proton.android.pass.data.api.PublicOkhttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WebauthnRelatedOriginsFetcher @Inject constructor(
    @param:PublicOkhttpClient private val okHttpClient: OkHttpClient
) : MobileWebauthnClientFetcher {

    override suspend fun fetch(url: String): MobileWebauthnDomainsResponse = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                when {
                    response.code == HTTP_NOT_FOUND -> throw MobileFetchException.NotFound(url)
                    !response.isSuccessful -> throw MobileFetchException.CannotFetch("HTTP ${response.code}")
                    response.body?.contentLength().let { it != null && it != -1L && it > MAX_BODY_SIZE } ->
                        throw MobileFetchException.CannotFetch("Response too large")
                    else -> parseBody(response.body?.string().orEmpty())
                }
            }
        }.getOrElse { error ->
            when (error) {
                is MobileFetchException -> throw error
                else -> throw MobileFetchException.CannotFetch(error.message ?: "network error")
            }
        }
    }

    @Suppress("SwallowedException")
    private fun parseBody(body: String): MobileWebauthnDomainsResponse {
        if (body.length > MAX_BODY_SIZE) throw MobileFetchException.CannotFetch("Response too large")
        return try {
            val parsed = lenientJson.decodeFromString(WebauthnDocument.serializer(), body)
            MobileWebauthnDomainsResponse(parsed.origins)
        } catch (e: SerializationException) {
            throw MobileFetchException.CannotFetch(e.message ?: "parse error")
        } catch (e: IllegalArgumentException) {
            throw MobileFetchException.CannotFetch(e.message ?: "parse error")
        }
    }

    @Serializable
    private data class WebauthnDocument(
        @SerialName("origins") val origins: List<String>
    )

    private companion object {
        private const val HTTP_NOT_FOUND = 404
        private const val MAX_BODY_SIZE = 512 * 1024 // 512 KB — ample for an origins list
        private val lenientJson = Json { ignoreUnknownKeys = true }
    }
}
