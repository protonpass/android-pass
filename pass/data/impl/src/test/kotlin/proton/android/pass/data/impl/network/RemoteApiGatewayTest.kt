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

package proton.android.pass.data.impl.network

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.TimeoutOverride
import org.junit.Assert.assertThrows
import org.junit.Test
import proton.android.pass.data.impl.fakes.FakeRemoteApiGateway
import proton.android.pass.domain.UserId

class RemoteApiGatewayTest {

    private interface TestApi : BaseRetrofitApi {
        suspend fun ping(): Int
    }

    private val testApi = object : TestApi {
        override suspend fun ping(): Int = PING_VALUE
        override suspend fun ping(timeoutOverride: TimeoutOverride) = Unit
    }

    @Test
    fun `success maps to success`() {
        val result = ApiResult.Success(PING_VALUE).toRemoteResult()

        assertThat(result).isEqualTo(RemoteResult.Success(PING_VALUE))
    }

    @Test
    fun `http error keeps status, proton code and proton message`() {
        val cause = IllegalStateException("boom")
        val core = ApiResult.Error.Http(
            httpCode = HTTP_UNPROCESSABLE,
            message = "http message",
            proton = ApiResult.Error.ProtonData(code = PROTON_CODE, error = "proton message"),
            cause = cause
        )

        val result = core.toRemoteResult()

        assertThat(result).isEqualTo(
            RemoteResult.Error.Http(
                status = HTTP_UNPROCESSABLE,
                protonCode = PROTON_CODE,
                message = "proton message",
                cause = cause
            )
        )
    }

    @Test
    fun `http error without proton data falls back to http message`() {
        val core = ApiResult.Error.Http(httpCode = HTTP_SERVER_ERROR, message = "http message")

        val result = core.toRemoteResult()

        assertThat(result).isEqualTo(
            RemoteResult.Error.Http(
                status = HTTP_SERVER_ERROR,
                protonCode = null,
                message = "http message",
                cause = null,
                isRetryable = true
            )
        )
    }

    @Test
    fun `no internet maps to no internet`() {
        val cause = IllegalStateException("offline")

        val result = ApiResult.Error.NoInternet(cause).toRemoteResult()

        assertThat(result).isEqualTo(RemoteResult.Error.NoInternet(cause))
    }

    @Test
    fun `other errors map to failure`() {
        val cause = IllegalStateException("parse")

        assertThat(ApiResult.Error.Parse(cause).toRemoteResult())
            .isEqualTo(RemoteResult.Error.Failure(cause, isRetryable = false))
        assertThat(ApiResult.Error.Timeout(true, cause).toRemoteResult())
            .isEqualTo(RemoteResult.Error.Failure(cause, isRetryable = true))
    }

    @Test
    fun `retryable flag follows core rules for http errors`() {
        val retryable = listOf(408, 421, 429, 500, 503).map {
            ApiResult.Error.Http(httpCode = it, message = "m").toRemoteResult()
        }
        val notRetryable = listOf(400, 401, 422).map {
            ApiResult.Error.Http(httpCode = it, message = "m").toRemoteResult()
        }

        assertThat(retryable.map { (it as RemoteResult.Error).isRetryable }).doesNotContain(false)
        assertThat(notRetryable.map { (it as RemoteResult.Error).isRetryable }).doesNotContain(true)
    }

    @Test
    fun `exception exposes the retryable flag of its error`() {
        assertThat(RemoteApiException(RemoteResult.Error.NoInternet()).isRetryable).isTrue()
        assertThat(RemoteApiException(RemoteResult.Error.Failure()).isRetryable).isFalse()
    }

    @Test
    fun `value or throw returns the success value`() {
        assertThat(RemoteResult.Success(PING_VALUE).valueOrThrow).isEqualTo(PING_VALUE)
    }

    @Test
    fun `value or throw throws a remote api exception carrying the error`() {
        val error = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, PROTON_CODE, "proton message")
        val result: RemoteResult<Int> = error

        val exception = assertThrows(RemoteApiException::class.java) { result.valueOrThrow }

        assertThat(exception.error).isEqualTo(error)
        assertThat(exception.message).isEqualTo("proton message")
    }

    @Test
    fun `proton error code is read from remote api exception`() {
        val exception = RemoteApiException(RemoteResult.Error.Http(HTTP_UNPROCESSABLE, PROTON_CODE, "message"))

        assertThat(exception.getProtonErrorCode()).isEqualTo(PROTON_CODE)
    }

    @Test
    fun `proton error code is read from core api exception`() {
        val exception = ApiException(
            ApiResult.Error.Http(
                httpCode = HTTP_UNPROCESSABLE,
                message = "message",
                proton = ApiResult.Error.ProtonData(code = PROTON_CODE, error = "error")
            )
        )

        assertThat(exception.getProtonErrorCode()).isEqualTo(PROTON_CODE)
    }

    @Test
    fun `proton error code is null for non http errors`() {
        assertThat(RemoteApiException(RemoteResult.Error.NoInternet()).getProtonErrorCode()).isNull()
        assertThat(ApiException(ApiResult.Error.NoInternet()).getProtonErrorCode()).isNull()
        assertThat(IllegalStateException().getProtonErrorCode()).isNull()
    }

    @Test
    fun `fake gateway runs the block on the registered api and records the request`() = runTest {
        val gateway = FakeRemoteApiGateway()
        gateway.setApi(TestApi::class, testApi)
        val userId = UserId("user-id")

        val result = gateway.get<TestApi>(userId).invoke { ping() }

        assertThat(result).isEqualTo(RemoteResult.Success(PING_VALUE))
        assertThat(gateway.requests).containsExactly(FakeRemoteApiGateway.Request(TestApi::class, userId))
    }

    @Test
    fun `fake gateway returns the configured error`() = runTest {
        val gateway = FakeRemoteApiGateway()
        gateway.setApi(TestApi::class, testApi)
        val error = RemoteResult.Error.NoInternet()
        gateway.error = error

        val result = gateway.get<TestApi>().invoke { ping() }

        assertThat(result).isEqualTo(error)
        assertThat(gateway.requests).containsExactly(FakeRemoteApiGateway.Request(TestApi::class, null))
    }

    private companion object {
        private const val PING_VALUE = 42
        private const val PROTON_CODE = 2_011
        private const val HTTP_UNPROCESSABLE = 422
        private const val HTTP_SERVER_ERROR = 500
    }
}
