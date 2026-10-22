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

package proton.android.pass.payment

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import me.proton.android.payment.capability.HttpCapability
import me.proton.android.payment.capability.model.HttpResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.GetPrimaryUserId
import proton.android.pass.data.impl.core.api.ProtonDynamicApi
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteApiGateway
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.data.impl.network.get
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpCapabilityImpl @Inject constructor(
    private val getPrimaryUserId: GetPrimaryUserId,
    private val apiGateway: RemoteApiGateway
) : HttpCapability {

    override suspend fun get(endpoint: String): HttpResponse = execute { get(endpoint.requireRelative()) }

    override suspend fun post(endpoint: String, body: ByteArray?): HttpResponse =
        execute { post(endpoint.requireRelative(), (body ?: ByteArray(0)).toRequestBody(JSON_MEDIA_TYPE)) }

    private suspend fun execute(block: suspend ProtonDynamicApi.() -> Response<ResponseBody>): HttpResponse {
        val userId = getPrimaryUserId()
            ?: error("No primary user for HTTP capability")
        return apiGateway.get<ProtonDynamicApi>(userId)
            .invoke { block() }
            .toHttpResponse()
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}

internal suspend fun RemoteResult<Response<ResponseBody>>.toHttpResponse(): HttpResponse = when (this) {
    is RemoteResult.Success -> {
        val response = value
        val bodyString = response.body()?.string() ?: response.errorBody()?.string().orEmpty()
        val body = safeRunCatching { Json.parseToJsonElement(bodyString).jsonObject }
            .getOrDefault(JsonObject(emptyMap()))
        HttpResponse(status = response.code(), body = body)
    }
    is RemoteResult.Error.Http -> HttpResponse(status = status, body = details ?: JsonObject(emptyMap()))
    is RemoteResult.Error -> throw RemoteApiException(this)
}

private fun String.requireRelative(): String {
    require(!startsWith("http://") && !startsWith("https://")) {
        "Absolute URLs are not allowed in HttpCapability: $this"
    }
    return this
}
