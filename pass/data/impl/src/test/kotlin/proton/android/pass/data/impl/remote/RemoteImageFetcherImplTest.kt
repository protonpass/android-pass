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

package proton.android.pass.data.impl.remote

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import proton.android.pass.data.impl.fakes.FakeRemoteApiGateway
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.domain.UserId

class RemoteImageFetcherImplTest {

    private val gateway = FakeRemoteApiGateway()
    private val instance = RemoteImageFetcherImpl(gateway)

    @Test
    fun `size not found at both sizes emits null after two requests`() = runTest {
        gateway.error = http(SIZE_NOT_FOUND)

        val result = fetch()

        assertThat(result).containsExactly(null)
        assertThat(gateway.invokeCount).isEqualTo(2)
    }

    @Test
    fun `size not found then not trusted emits null`() = runTest {
        gateway.enqueueError(http(SIZE_NOT_FOUND))
        gateway.enqueueError(http(NOT_TRUSTED))

        val result = fetch()

        assertThat(result).containsExactly(null)
        assertThat(gateway.invokeCount).isEqualTo(2)
    }

    @Test
    fun `known codes emit null without trying another size`() = runTest {
        listOf(NOT_TRUSTED, INVALID_ADDRESS, FAILED_TO_FIND).forEach { code ->
            val fetcher = RemoteImageFetcherImpl(FakeRemoteApiGateway().apply { error = http(code) })

            val result = fetcher.fetchFavicon(USER_ID, DOMAIN).toList()

            assertThat(result).containsExactly(null)
        }
    }

    @Test
    fun `known code stops after the first request`() = runTest {
        gateway.error = http(FAILED_TO_FIND)

        fetch()

        assertThat(gateway.invokeCount).isEqualTo(1)
    }

    @Test
    fun `unknown proton code surfaces a RemoteApiException`() = runTest {
        gateway.error = http(OTHER_CODE)

        val thrown = assertThrows(RemoteApiException::class.java) { runTestFetch() }

        assertThat(thrown.error).isEqualTo(gateway.error)
    }

    @Test
    fun `non unprocessable status surfaces a RemoteApiException`() = runTest {
        gateway.error = RemoteResult.Error.Http(HTTP_SERVER_ERROR, SIZE_NOT_FOUND, "message")

        assertThrows(RemoteApiException::class.java) { runTestFetch() }
    }

    @Test
    fun `non http errors surface a RemoteApiException`() = runTest {
        gateway.error = RemoteResult.Error.NoInternet()

        val thrown = assertThrows(RemoteApiException::class.java) { runTestFetch() }

        assertThat(thrown.isRetryable).isTrue()
    }

    private suspend fun fetch() = instance.fetchFavicon(USER_ID, DOMAIN).toList()

    private fun runTestFetch() = runBlocking { fetch() }

    private fun http(protonCode: Int) = RemoteResult.Error.Http(HTTP_UNPROCESSABLE, protonCode, "message")

    private companion object {
        private val USER_ID = UserId("user-id")
        private const val DOMAIN = "example.com"
        private const val NOT_TRUSTED = 2_011
        private const val INVALID_ADDRESS = -1
        private const val SIZE_NOT_FOUND = 2_511
        private const val FAILED_TO_FIND = 2_902
        private const val OTHER_CODE = 2_000
        private const val HTTP_UNPROCESSABLE = 422
        private const val HTTP_SERVER_ERROR = 500
    }
}
