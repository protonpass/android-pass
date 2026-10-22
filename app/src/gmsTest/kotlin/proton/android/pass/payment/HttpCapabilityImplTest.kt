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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import retrofit2.Response

class HttpCapabilityImplTest {

    @Test
    fun `maps successful response status and parsed body`() = runBlocking {
        val response = Response.success(201, "{\"payment\":\"accepted\"}".toResponseBody())

        val result = RemoteResult.Success(response).toHttpResponse()

        assertThat(result.status).isEqualTo(201)
        assertThat(result.body).isEqualTo(Json.parseToJsonElement("{\"payment\":\"accepted\"}").jsonObject)
    }

    @Test
    fun `maps HTTP error status and Proton details`() = runBlocking {
        val details = Json.parseToJsonElement("{\"RetryAfter\":10}").jsonObject
        val error = RemoteResult.Error.Http(
            status = 429,
            protonCode = 0,
            message = "Too many requests",
            details = details
        )

        val result = (error as RemoteResult<Response<ResponseBody>>).toHttpResponse()

        assertThat(result.status).isEqualTo(429)
        assertThat(result.body).isEqualTo(details)
    }

    @Test
    fun `throws RemoteApiException for non HTTP error`() = runBlocking {
        val apiError = RemoteResult.Error.NoInternet()

        val exception = try {
            (apiError as RemoteResult<Response<ResponseBody>>).toHttpResponse()
            error("Expected RemoteApiException")
        } catch (exception: RemoteApiException) {
            exception
        }

        assertThat(exception.error).isSameInstanceAs(apiError)
    }
}
