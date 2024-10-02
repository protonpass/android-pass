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
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteAssetLinkDataSourceImpl @Inject constructor(
    @PublicOkhttpClient private val okHttpClient: OkHttpClient
) : RemoteAssetLinkDataSource {

    override suspend fun fetch(website: String): List<AssetLinkResponse> = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder()
            .url("$website/.well-known/assetlinks.json")
            .build()
        val call = okHttpClient.newCall(request)
        continuation.invokeOnCancellation {
            call.cancel()
        }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (continuation.isActive) {
                    if (response.isSuccessful) {
                        val json = response.body?.string()
                        when {
                            json.isNullOrEmpty() ->
                                continuation.resumeWithException(IllegalStateException("Empty response"))

                            else ->
                                runCatching {
                                    val assetLinks =
                                        Json.decodeFromString<List<AssetLinkResponse>>(json)
                                    continuation.resume(assetLinks)
                                }.onFailure(continuation::resumeWithException)
                        }
                    } else {
                        continuation.resumeWithException(IOException("Unexpected code $response"))
                    }
                }
            }
        })
    }
}
