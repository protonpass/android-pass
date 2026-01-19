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

package proton.android.pass.data.impl.remote.assetlink

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import proton.android.pass.data.api.errors.ResponseSizeExceededError
import proton.android.pass.data.impl.remote.PublicOkhttpClient
import proton.android.pass.data.impl.responses.AssetLinkResponse
import proton.android.pass.data.impl.responses.IgnoredAssetLinkResponse
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteAssetLinkDataSourceImpl @Inject constructor(
    @param:PublicOkhttpClient private val okHttpClient: OkHttpClient
) : RemoteAssetLinkDataSource {

    override suspend fun fetch(website: String): List<AssetLinkResponse> {
        val url = "$website/.well-known/assetlinks.json"
        return makeRequest(url) { json ->
            Json.decodeFromString<List<AssetLinkResponse>>(json)
        }
    }

    override suspend fun fetchIgnored(): IgnoredAssetLinkResponse {
        return makeRequest(DENIED_ASSET_LINKS_URL) { json ->
            Json.decodeFromString<IgnoredAssetLinkResponse>(json)
        }
    }

    private suspend fun <T> makeRequest(url: String, parse: (String) -> T): T =
        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder().url(url).build()
            val call = okHttpClient.newCall(request)

            continuation.invokeOnCancellation { call.cancel() }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!continuation.isActive) {
                        response.close()
                        return
                    }
                    response.use { handleResponse(it, continuation, parse) }
                }
            })
        }

    private fun <T> handleResponse(
        response: Response,
        continuation: CancellableContinuation<T>,
        parse: (String) -> T
    ) {
        if (!response.isSuccessful) {
            continuation.resumeWithException(IOException("Unexpected response code $response"))
            return
        }

        readAndValidateBody(response).fold(
            onSuccess = { json ->
                runCatching {
                    parse(json)
                }.onSuccess(continuation::resume)
                    .onFailure { e ->
                        PassLogger.w(TAG, "Failed to parse response")
                        PassLogger.w(TAG, e)
                        continuation.resumeWithException(e)
                    }
            },
            onFailure = { e ->
                continuation.resumeWithException(e)
            }
        )
    }

    private fun readAndValidateBody(response: Response): Result<String> {
        // Check Content-Length header BEFORE reading body
        val contentLength = response.body?.contentLength() ?: -1
        if (contentLength > MAX_RESPONSE_SIZE_BYTES) {
            return createSizeExceededError(response.request.url.toString(), contentLength)
        }

        // Read and validate response body
        return try {
            val bodyString = response.body?.string().orEmpty()

            when {
                bodyString.isEmpty() -> {
                    Result.failure(IllegalStateException("Empty response"))
                }
                bodyString.length > MAX_RESPONSE_SIZE_BYTES -> {
                    createSizeExceededError(response.request.url.toString(), bodyString.length.toLong())
                }
                else -> Result.success(bodyString)
            }
        } catch (e: IOException) {
            PassLogger.w(TAG, "Failed to read response body")
            PassLogger.w(TAG, e)
            Result.failure(e)
        }
    }

    private fun createSizeExceededError(url: String, contentLength: Long): Result<String> {
        PassLogger.w(TAG, "Response size exceeds maximum allowed")
        return Result.failure(
            ResponseSizeExceededError(
                url = url,
                contentLength = contentLength,
                maxSize = MAX_RESPONSE_SIZE_BYTES
            )
        )
    }

    companion object {
        private const val TAG = "RemoteAssetLinkDataSource"
        private const val MAX_RESPONSE_SIZE_BYTES = 2 * 1024 * 1024L
    }
}

private const val DENIED_ASSET_LINKS_URL = "https://proton.me/download/pass/digital-asset-links/rules.json"
