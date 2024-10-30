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
import proton.android.pass.data.impl.remote.PublicOkhttpClient
import proton.android.pass.data.impl.responses.AssetLinkResponse
import proton.android.pass.data.impl.responses.IgnoredAssetLinkResponse
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteAssetLinkDataSourceImpl @Inject constructor(
    @PublicOkhttpClient private val okHttpClient: OkHttpClient
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
                    if (!continuation.isActive) return
                    handleResponse(response, continuation, parse)
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

        val json = response.body?.string().orEmpty()
        if (json.isEmpty()) {
            continuation.resumeWithException(IllegalStateException("Empty response"))
            return
        }

        runCatching {
            parse(json)
        }.onSuccess(continuation::resume)
            .onFailure(continuation::resumeWithException)
    }
}

private const val DENIED_ASSET_LINKS_URL = "https://proton.me/download/pass/digital-asset-links/rules.json"
