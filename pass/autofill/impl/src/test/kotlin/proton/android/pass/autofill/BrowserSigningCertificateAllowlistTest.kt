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

package proton.android.pass.autofill

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BrowserSigningCertificateAllowlistTest {

    @Test
    fun `parses package name to set of release-signed fingerprints only`() {
        val json = """
            {
              "apps": [
                {
                  "type": "android",
                  "info": {
                    "package_name": "com.android.chrome",
                    "signatures": [
                      { "build": "release", "cert_fingerprint_sha256": "AA:BB" },
                      { "build": "userdebug", "cert_fingerprint_sha256": "CC:DD" }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val result = BrowserSigningCertificateAllowlist.parse(json)

        assertThat(result).containsExactly("com.android.chrome", setOf("AA:BB"))
    }

    @Test
    fun `drops a package left with no release-signed fingerprints`() {
        val json = """
            {
              "apps": [
                {
                  "type": "android",
                  "info": {
                    "package_name": "com.google.android.apps.chrome",
                    "signatures": [
                      { "build": "userdebug", "cert_fingerprint_sha256": "CC:DD" }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val result = BrowserSigningCertificateAllowlist.parse(json)

        assertThat(result).isEmpty()
    }

    @Test
    fun `returns empty map for malformed json`() {
        assertThat(BrowserSigningCertificateAllowlist.parse("not json")).isEmpty()
    }
}
