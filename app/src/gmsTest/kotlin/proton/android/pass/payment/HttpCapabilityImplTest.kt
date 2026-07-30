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
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response

class HttpCapabilityImplTest {

    @Test
    fun `maps successful response status and parsed body`() = runBlocking {
        val response = Response.success(201, "{\"payment\":\"accepted\"}".toResponseBody())

        val result = ApiResult.Success(response).toHttpResponse()

        assertThat(result.status).isEqualTo(201)
        assertThat(result.body).isEqualTo(Json.parseToJsonElement("{\"payment\":\"accepted\"}").jsonObject)
    }

    @Test
    fun `maps HTTP error status and Proton details`() = runBlocking {
        val details = Json.parseToJsonElement("{\"RetryAfter\":10}").jsonObject
        val error = ApiResult.Error.Http(
            httpCode = 429,
            message = "Too many requests",
            proton = ApiResult.Error.ProtonData(code = 0, error = "", details = details),
        )

        val result = (error as ApiResult<Response<ResponseBody>>).toHttpResponse()

        assertThat(result.status).isEqualTo(429)
        assertThat(result.body).isEqualTo(details)
    }

    @Test
    fun `throws ApiException for non HTTP error`() = runBlocking {
        val apiError = ApiResult.Error.NoInternet()

        val exception = try {
            (apiError as ApiResult<Response<ResponseBody>>).toHttpResponse()
            error("Expected ApiException")
        } catch (exception: ApiException) {
            exception
        }

        assertThat(exception.error).isSameInstanceAs(apiError)
    }
}
