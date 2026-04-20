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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class VerifyDigitalAssetLinksForCredentialSharingImplTest {

    private lateinit var server: MockWebServer
    private lateinit var verifier: VerifyDigitalAssetLinksForCredentialSharingImpl

    private val testPackage = "com.example.app"

    // 32-byte hex fingerprint in colon-separated uppercase — same format DAL files use.
    private val fingerprintColonHex = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:" +
        "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val client = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .build()
        verifier = VerifyDigitalAssetLinksForCredentialSharingImpl(client)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun serverBaseUrl(): String = server.url("/").toString().trimEnd('/')

    private fun assetLinksJson(
        relations: List<String> = listOf("delegate_permission/common.get_login_creds"),
        namespace: String = "android_app",
        packageName: String = testPackage,
        fingerprints: List<String> = listOf(fingerprintColonHex)
    ): String {
        val relJson = relations.joinToString(",") { "\"$it\"" }
        val fpJson = fingerprints.joinToString(",") { "\"$it\"" }
        val json = """
            |{
            |"relation":[$relJson],
            |"target":{
            |"namespace":"$namespace",
            |"package_name":"$packageName",
            |"sha256_cert_fingerprints":[$fpJson]
            |}
            |}
        """.trimMargin().trim()
        return "[$json]"
    }

    // ------------------------------------------------------------------
    // Success paths — all validation checks pass
    // ------------------------------------------------------------------

    @Test
    fun `returns true when relation, package, and fingerprint all match`() = runTest {
        server.enqueue(MockResponse().setBody(assetLinksJson()))

        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = setOf(fingerprintColonHex)
        )

        assertThat(result).isTrue()
    }

    @Test
    fun `returns true when fingerprint matches after colon stripping and case normalisation`() = runTest {
        val callerFp = fingerprintColonHex.replace(":", "").lowercase()
        server.enqueue(MockResponse().setBody(assetLinksJson()))

        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = setOf(callerFp)
        )

        assertThat(result).isTrue()
    }

    // ------------------------------------------------------------------
    // Identity / verification failure paths
    // ------------------------------------------------------------------

    @Test
    fun `returns false when caller provides empty fingerprint set`() = runTest {
        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = emptySet()
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false when package name differs`() = runTest {
        server.enqueue(MockResponse().setBody(assetLinksJson()))

        val result = verifier(
            website = serverBaseUrl(),
            packageName = "com.attacker.app",
            certificateFingerprints = setOf(fingerprintColonHex)
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false when certificate fingerprint differs`() = runTest {
        server.enqueue(MockResponse().setBody(assetLinksJson()))
        val differentFp = "11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:" +
            "11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00"

        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = setOf(differentFp)
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false when namespace is not android_app`() = runTest {
        server.enqueue(MockResponse().setBody(assetLinksJson(namespace = "web")))

        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = setOf(fingerprintColonHex)
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false when only handle_all_urls relation present`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                assetLinksJson(
                    relations = listOf("delegate_permission/common.handle_all_urls")
                )
            )
        )

        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = setOf(fingerprintColonHex)
        )

        assertThat(result).isFalse()
    }

    // ------------------------------------------------------------------
    // Network / response failures
    // ------------------------------------------------------------------

    @Test
    fun `returns false on non-200 response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = setOf(fingerprintColonHex)
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false on malformed JSON`() = runTest {
        server.enqueue(MockResponse().setBody("this is not json at all"))

        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = setOf(fingerprintColonHex)
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false when response body exceeds 2 MB`() = runTest {
        // 2 MB + 1 byte exceeds MAX_RESPONSE_SIZE_BYTES.
        val oversizedBody = "x".repeat(2 * 1024 * 1024 + 1)
        server.enqueue(MockResponse().setBody(oversizedBody))

        val result = verifier(
            website = serverBaseUrl(),
            packageName = testPackage,
            certificateFingerprints = setOf(fingerprintColonHex)
        )

        assertThat(result).isFalse()
    }

    @Test
    fun `returns false when server is unreachable`() = runTest {
        val unreachableServer = MockWebServer()
        unreachableServer.start()
        val baseUrl = unreachableServer.url("/").toString().trimEnd('/')
        unreachableServer.shutdown()

        val result = verifier(
            website = baseUrl,
            packageName = testPackage,
            certificateFingerprints = setOf(fingerprintColonHex)
        )

        assertThat(result).isFalse()
    }
}
