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

package proton.android.pass.data.impl.remote.assetlink

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.errors.ResponseSizeExceededError
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class RemoteAssetLinkDataSourceImplTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RemoteAssetLinkDataSourceImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        dataSource = RemoteAssetLinkDataSourceImpl(OkHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `rejects a compressed response before fully reading its decoded body`() = runTest {
        val decodedBody = "[${" ".repeat(MAX_RESPONSE_SIZE_BYTES.toInt() + 1024 * 1024)}]"
        server.enqueue(
            MockResponse()
                .setHeader("Content-Encoding", "gzip")
                .setBody(Buffer().write(gzip(decodedBody)))
        )

        val failure = runCatching { dataSource.fetch(serverBaseUrl()) }.exceptionOrNull()

        assertThat(failure).isInstanceOf(ResponseSizeExceededError::class.java)
        val sizeError = failure as ResponseSizeExceededError
        assertThat(sizeError.contentLength).isAtMost(MAX_RESPONSE_SIZE_BYTES + MAX_READ_AHEAD_BYTES)
    }

    @Test
    fun `returns only Android app statements from the streamed response`() = runTest {
        val responseBody = """
            |[
            |  {
            |    "relation": ["delegate_permission/common.handle_all_urls"],
            |    "target": {
            |      "namespace": "web",
            |      "site": "https://irrelevant.example"
            |    }
            |  },
            |  {
            |    "relation": ["delegate_permission/common.handle_all_urls"],
            |    "target": {
            |      "namespace": "android_app",
            |      "package_name": "com.example.app",
            |      "sha256_cert_fingerprints": ["fingerprint-a", "fingerprint-b"]
            |    }
            |  }
            |]
        """.trimMargin()
        server.enqueue(
            MockResponse().setBody(responseBody)
        )

        val responses = dataSource.fetch(serverBaseUrl())

        assertThat(responses).hasSize(1)
        assertThat(responses.single().target.packageName).isEqualTo("com.example.app")
        assertThat(responses.single().target.sha256CertFingerprints)
            .containsExactly("fingerprint-a", "fingerprint-b")
    }

    private fun serverBaseUrl(): String = server.url("/").toString().trimEnd('/')

    private fun gzip(content: String): ByteArray = ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).bufferedWriter().use { writer -> writer.write(content) }
        output.toByteArray()
    }

    private companion object {
        const val MAX_RESPONSE_SIZE_BYTES = 128 * 1024L
        const val MAX_READ_AHEAD_BYTES = 8 * 1024L
    }
}
