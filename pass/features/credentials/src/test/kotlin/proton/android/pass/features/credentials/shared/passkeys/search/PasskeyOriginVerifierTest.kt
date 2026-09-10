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

package proton.android.pass.features.credentials.shared.passkeys.search

import kotlinx.coroutines.test.runTest
import proton.android.pass.browserallowlist.api.PrivilegedBrowserAllowlistProvider
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.usecases.VerifyDigitalAssetLinksForCredentialSharing
import proton.android.pass.data.fakes.url.FakeHostParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PasskeyOriginVerifierTest {

    // --- Helper fakes ---

    private fun fakeDigitalAssetLinksVerifier(): VerifyDigitalAssetLinksForCredentialSharing {
        return object : VerifyDigitalAssetLinksForCredentialSharing {
            override suspend fun invoke(
                website: String,
                packageName: String,
                certificateFingerprints: Set<String>
            ): Boolean = true
        }
    }

    private fun fakeAllowlistProvider(): PrivilegedBrowserAllowlistProvider {
        return object : PrivilegedBrowserAllowlistProvider {
            override val json: String
                get() = """
                    {
                      "apps": [
                        {
                          "type": "android",
                          "info": {
                            "package_name": "com.android.chrome",
                            "signatures": [
                              {
                                "cert_fingerprint_sha256": "AA:BB:CC:DD"
                              }
                            ]
                          }
                        }
                      ]
                    }
                """.trimIndent()
        }
    }

    private fun fakeHostParser(result: Result<HostInfo>): FakeHostParser = FakeHostParser().apply { setResult(result) }

    // --- extractHost ---

    @Test
    fun `extractHost returns host from https origin`() {
        assertEquals("example.com", PasskeyOriginVerifier.extractHost("https://example.com"))
    }

    @Test
    fun `extractHost returns host from origin with port`() {
        assertEquals("example.com", PasskeyOriginVerifier.extractHost("https://example.com:443"))
    }

    @Test
    fun `extractHost returns host from origin with path`() {
        assertEquals("example.com", PasskeyOriginVerifier.extractHost("https://example.com/path"))
    }

    @Test
    fun `extractHost returns null for invalid URI`() {
        assertNull(PasskeyOriginVerifier.extractHost("not a uri"))
    }

    @Test
    fun `extractHost returns null for empty string`() {
        assertNull(PasskeyOriginVerifier.extractHost(""))
    }

    // --- isDomainMatch ---

    @Test
    fun `isDomainMatch returns true for exact match`() {
        assertTrue(PasskeyOriginVerifier.isDomainMatch("example.com", "example.com"))
    }

    @Test
    fun `isDomainMatch returns true for exact match case insensitive`() {
        assertTrue(PasskeyOriginVerifier.isDomainMatch("Example.COM", "example.com"))
    }

    @Test
    fun `isDomainMatch returns true for subdomain of rpId`() {
        assertTrue(PasskeyOriginVerifier.isDomainMatch("www.example.com", "example.com"))
    }

    @Test
    fun `isDomainMatch returns true for deep subdomain of rpId`() {
        assertTrue(PasskeyOriginVerifier.isDomainMatch("auth.login.example.com", "example.com"))
    }

    @Test
    fun `isDomainMatch returns false for different domain`() {
        assertFalse(PasskeyOriginVerifier.isDomainMatch("evil.com", "example.com"))
    }

    @Test
    fun `isDomainMatch returns false for suffix attack`() {
        assertFalse(PasskeyOriginVerifier.isDomainMatch("notexample.com", "example.com"))
    }

    @Test
    fun `isDomainMatch returns false for prefix attack`() {
        assertFalse(PasskeyOriginVerifier.isDomainMatch("example.com.evil.com", "example.com"))
    }

    @Test
    fun `isDomainMatch returns false when rpId is subdomain of host`() {
        assertFalse(PasskeyOriginVerifier.isDomainMatch("example.com", "www.example.com"))
    }

    // --- verifyOrigin method tests ---

    @Test
    fun `verifyOrigin returns null when callingAppInfo is null`() = runTest {
        val hostInfo = HostInfo.Host(protocol = "https", subdomain = None, domain = "example", tld = Some("com"))
        val verifier = PasskeyOriginVerifier(
            verifyDigitalAssetLinksForCredentialSharing = fakeDigitalAssetLinksVerifier(),
            privilegedBrowserAllowlistProvider = fakeAllowlistProvider(),
            hostParser = fakeHostParser(Result.success(hostInfo))
        )
        val result = verifier.verifyOrigin(
            callingAppInfo = null,
            requestedRpId = "example.com"
        )
        assertNull(result)
    }

    // ------------------------------------------------------------------
    // isValidRpId
    // ------------------------------------------------------------------

    @Test
    fun `isValidRpId accepts simple hostname`() {
        assertTrue(PasskeyOriginVerifier.isValidRpId("example.com"))
    }

    @Test
    fun `isValidRpId accepts subdomain`() {
        assertTrue(PasskeyOriginVerifier.isValidRpId("sub.example.com"))
    }

    @Test
    fun `isValidRpId accepts hostname with hyphen`() {
        assertTrue(PasskeyOriginVerifier.isValidRpId("my-app.example.com"))
    }

    @Test
    fun `isValidRpId rejects empty string`() {
        assertFalse(PasskeyOriginVerifier.isValidRpId(""))
    }

    @Test
    fun `isValidRpId rejects userinfo injection`() {
        assertFalse(PasskeyOriginVerifier.isValidRpId("attacker.com@victim.com"))
    }

    @Test
    fun `isValidRpId rejects rpId with path`() {
        assertFalse(PasskeyOriginVerifier.isValidRpId("example.com/path"))
    }

    @Test
    fun `isValidRpId rejects rpId with port`() {
        assertFalse(PasskeyOriginVerifier.isValidRpId("example.com:8080"))
    }

    @Test
    fun `isValidRpId rejects rpId with scheme`() {
        assertFalse(PasskeyOriginVerifier.isValidRpId("https://example.com"))
    }

    @Test
    fun `isValidRpId rejects rpId with query`() {
        assertFalse(PasskeyOriginVerifier.isValidRpId("example.com?foo=bar"))
    }

    @Test
    fun `isValidRpId rejects rpId with fragment`() {
        assertFalse(PasskeyOriginVerifier.isValidRpId("example.com#section"))
    }

    @Test
    fun `isRegistrableDomain accepts a host with subdomain and tld`() {
        val hostInfo = HostInfo.Host(protocol = "https", subdomain = None, domain = "example", tld = Some("com"))
        assertTrue(PasskeyOriginVerifier.isRegistrableDomain(fakeHostParser(Result.success(hostInfo)), "example.com"))
    }

    @Test
    fun `isRegistrableDomain rejects a bare public suffix`() {
        val parser = fakeHostParser(Result.failure(IllegalArgumentException("host is a TLD")))
        assertFalse(PasskeyOriginVerifier.isRegistrableDomain(parser, "co.uk"))
    }

    @Test
    fun `isRegistrableDomain rejects a single-label host with no tld`() {
        val hostInfo = HostInfo.Host(protocol = "https", subdomain = None, domain = "localhost", tld = None)
        assertFalse(PasskeyOriginVerifier.isRegistrableDomain(fakeHostParser(Result.success(hostInfo)), "localhost"))
    }

    @Test
    fun `isRegistrableDomain rejects an IP address`() {
        val parser = fakeHostParser(Result.success(HostInfo.Ip("1.2.3.4")))
        assertFalse(PasskeyOriginVerifier.isRegistrableDomain(parser, "1.2.3.4"))
    }

    @Test
    fun `isRegistrableDomain rejects an unparseable host`() {
        val parser = fakeHostParser(Result.success(HostInfo.Unparseable(protocol = "https", rawHost = "%")))
        assertFalse(PasskeyOriginVerifier.isRegistrableDomain(parser, "%"))
    }
}
