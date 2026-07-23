/*
 * Copyright (c) 2023-2026 Proton AG
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import proton.android.pass.data.api.PublicOkhttpClient
import proton.android.pass.data.api.errors.ResponseSizeExceededError
import proton.android.pass.data.impl.responses.AssetLinkResponse
import proton.android.pass.data.impl.responses.IgnoredAssetLinkResponse
import proton.android.pass.log.api.PassLogger
import java.io.InputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteAssetLinkDataSourceImpl @Inject constructor(
    @param:PublicOkhttpClient private val okHttpClient: OkHttpClient
) : RemoteAssetLinkDataSource {

    override suspend fun fetch(website: String): List<AssetLinkResponse> {
        val url = "$website/.well-known/assetlinks.json"
        return makeRequest(url, ::parseAssetLinkStatements)
    }

    override suspend fun fetchIgnored(): IgnoredAssetLinkResponse =
        makeRequest(DENIED_ASSET_LINKS_URL, ::parseIgnoredAssetLinks)

    private suspend fun <T> makeRequest(url: String, parse: (InputStream) -> T): T =
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
        parse: (InputStream) -> T
    ) {
        if (!response.isSuccessful) {
            continuation.resumeWithException(IOException("Unexpected response code $response"))
            return
        }

        readAndValidateBody(response, parse).fold(
            onSuccess = continuation::resume,
            onFailure = { e ->
                continuation.resumeWithException(e)
            }
        )
    }

    private fun <T> readAndValidateBody(response: Response, parse: (InputStream) -> T): Result<T> {
        // Check Content-Length header BEFORE reading body
        val contentLength = response.body?.contentLength() ?: -1
        if (contentLength > MAX_RESPONSE_SIZE_BYTES) {
            return Result.failure(createSizeExceededError(response.request.url.toString(), contentLength))
        }

        // OkHttp transparently decompresses gzip responses. Bound the decoded stream before parsing it.
        return runCatching {
            val boundedBody = BoundedInputStream(
                delegate = response.body?.byteStream()
                    ?: return Result.failure(IllegalStateException("Empty response")),
                url = response.request.url.toString(),
                maxBytes = MAX_RESPONSE_SIZE_BYTES
            )
            parse(boundedBody)
        }.onFailure { error ->
            when (error) {
                is ResponseSizeExceededError -> PassLogger.w(TAG, "Response size exceeds maximum allowed")
                is IOException -> PassLogger.w(TAG, "Failed to read response body")
                else -> PassLogger.w(TAG, "Failed to parse response")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun parseAssetLinkStatements(body: InputStream): List<AssetLinkResponse> =
        Json.decodeFromStream<List<AssetLinkResponse>>(body)
            .filter { it.target.namespace == ANDROID_APP_NAMESPACE }

    @OptIn(ExperimentalSerializationApi::class)
    private fun parseIgnoredAssetLinks(body: InputStream): IgnoredAssetLinkResponse =
        Json.decodeFromStream<IgnoredAssetLinkResponse>(body)

    private fun createSizeExceededError(url: String, contentLength: Long): ResponseSizeExceededError {
        PassLogger.w(TAG, "Response size exceeds maximum allowed")
        return ResponseSizeExceededError(
            url = url,
            contentLength = contentLength,
            maxSize = MAX_RESPONSE_SIZE_BYTES
        )
    }

    private class BoundedInputStream(
        private val delegate: InputStream,
        private val url: String,
        private val maxBytes: Long
    ) : InputStream() {
        var bytesRead = 0L
            private set

        override fun read(): Int = delegate.read().also { byte ->
            if (byte != -1) bump(1)
        }

        override fun read(
            buffer: ByteArray,
            offset: Int,
            length: Int
        ): Int = delegate.read(buffer, offset, length).also { count ->
            if (count > 0) bump(count.toLong())
        }

        override fun close() = delegate.close()

        private fun bump(count: Long) {
            bytesRead += count
            if (bytesRead > maxBytes) {
                throw ResponseSizeExceededError(
                    url = url,
                    contentLength = bytesRead,
                    maxSize = maxBytes
                )
            }
        }
    }

    companion object {
        private const val TAG = "RemoteAssetLinkDataSource"
        private const val MAX_RESPONSE_SIZE_BYTES = 128 * 1024L
        private const val ANDROID_APP_NAMESPACE = "android_app"
    }
}

private const val DENIED_ASSET_LINKS_URL = "https://proton.me/download/pass/digital-asset-links/rules.json"
