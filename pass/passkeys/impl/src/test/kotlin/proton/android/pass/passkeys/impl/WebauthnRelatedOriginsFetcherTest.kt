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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import proton.android.pass.commonrust.MobileFetchException

class WebauthnRelatedOriginsFetcherTest {

    private lateinit var server: MockWebServer
    private lateinit var fetcher: WebauthnRelatedOriginsFetcher

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        fetcher = WebauthnRelatedOriginsFetcher(OkHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun serverUrl(): String = server.url("/").toString().trimEnd('/')

    @Test
    fun `returns origins from valid well-known response`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"origins":["https://login.example.com","https://app.example.com"]}"""
            )
        )

        val result = fetcher.fetch(serverUrl())

        assertThat(result.origins).containsExactly(
            "https://login.example.com",
            "https://app.example.com"
        ).inOrder()
    }

    @Test
    fun `returns empty origins list when origins array is empty`() = runTest {
        server.enqueue(MockResponse().setBody("""{"origins":[]}"""))

        val result = fetcher.fetch(serverUrl())

        assertThat(result.origins).isEmpty()
    }

    @Test
    fun `throws NotFound on 404 response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val exception = runCatching { fetcher.fetch(serverUrl()) }.exceptionOrNull()

        assertThat(exception).isInstanceOf(MobileFetchException.NotFound::class.java)
    }

    @Test
    fun `throws CannotFetch on non-200 non-404 response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val exception = runCatching { fetcher.fetch(serverUrl()) }.exceptionOrNull()

        assertThat(exception).isInstanceOf(MobileFetchException.CannotFetch::class.java)
    }

    @Test
    fun `throws CannotFetch on malformed JSON`() = runTest {
        server.enqueue(MockResponse().setBody("this is not json"))

        val exception = runCatching { fetcher.fetch(serverUrl()) }.exceptionOrNull()

        assertThat(exception).isInstanceOf(MobileFetchException.CannotFetch::class.java)
    }

    @Test
    fun `throws CannotFetch when origins key is missing`() = runTest {
        server.enqueue(MockResponse().setBody("""{"something_else":[]}"""))

        val exception = runCatching { fetcher.fetch(serverUrl()) }.exceptionOrNull()

        assertThat(exception).isInstanceOf(MobileFetchException.CannotFetch::class.java)
    }

    @Test
    fun `throws CannotFetch when body exceeds size limit`() = runTest {
        val oversized = "x".repeat(512 * 1024 + 1)
        server.enqueue(MockResponse().setBody(oversized))

        val exception = runCatching { fetcher.fetch(serverUrl()) }.exceptionOrNull()

        assertThat(exception).isInstanceOf(MobileFetchException.CannotFetch::class.java)
    }

    @Test
    fun `throws CannotFetch on network failure`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

        val exception = runCatching { fetcher.fetch(serverUrl()) }.exceptionOrNull()

        assertThat(exception).isInstanceOf(MobileFetchException.CannotFetch::class.java)
    }
}
