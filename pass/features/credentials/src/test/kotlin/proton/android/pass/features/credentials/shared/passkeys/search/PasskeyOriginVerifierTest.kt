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
import proton.android.pass.data.api.usecases.VerifyDigitalAssetLinksForCredentialSharing
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
        val verifier = PasskeyOriginVerifier(
            verifyDigitalAssetLinksForCredentialSharing = fakeDigitalAssetLinksVerifier(),
            privilegedBrowserAllowlistProvider = fakeAllowlistProvider()
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

    // Note: verifyOrigin browser path (isOriginPopulated=true) and native-app path require
    // constructing CallingAppInfo with SigningInfo, which is a final Android platform class
    // with no public constructor. These paths cannot be tested in JVM unit tests without
    // Robolectric. The security properties of these paths are enforced by:
    // 1. isDomainMatch / extractHost contract — tested exhaustively above
    // 2. SigningCertificateFingerprints.hasSingleSigner rejecting empty/multi-signer — tested in
    //    pass/signing-certificates SigningCertificateFingerprintsTest
    // 3. Live DAL check returning false for unauthorized callers — tested in
    //    VerifyDigitalAssetLinksForCredentialSharingImplTest
    // Integration coverage is provided by manual QA with the credential manager flow.

    // Allowlist resource content is now tested where the resource lives:
    // pass/browser-allowlist/impl/.../PrivilegedBrowserAllowlistContentTest.kt
}
