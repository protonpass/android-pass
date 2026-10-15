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

package proton.android.pass.browserallowlist.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrivilegedBrowserAllowlistContentTest {

    @Serializable
    private data class Signature(@SerialName("cert_fingerprint_sha256") val certFingerprintSha256: String)

    @Serializable
    private data class AppInfo(
        @SerialName("package_name") val packageName: String,
        val signatures: List<Signature>
    )

    @Serializable
    private data class App(val type: String, val info: AppInfo)

    @Serializable
    private data class Allowlist(val apps: List<App>)

    @Test
    fun `allowlist resource JSON is valid and contains expected AndroidX structure`() {
        val json = allowlistFile().readText()
        val allowlist = Json { ignoreUnknownKeys = true }.decodeFromString<Allowlist>(json)

        assertTrue(allowlist.apps.isNotEmpty())
        val first = allowlist.apps.first()
        assertEquals("android", first.type)
        assertTrue(first.info.packageName.isNotEmpty())
        assertTrue(first.info.signatures.isNotEmpty())
        val fp = first.info.signatures.first().certFingerprintSha256
        assertTrue(
            fp.matches(Regex("([0-9A-Fa-f]{2}:){31}[0-9A-Fa-f]{2}")),
            "Fingerprint is not valid colon-hex SHA-256: $fp"
        )
    }

    @Test
    fun `allowlist resource contains no publicly-known Android test-key signers`() {
        // Public AOSP test keys have publicly-distributed private keys, so any app could forge
        // them to claim a privileged web origin. They must never be trusted signers. Each value
        // is the SHA-256 of a checked-in AOSP test cert (build/target/product/security/*.x509.pem):
        // platform, testkey, media, shared.
        val publicTestKeyFingerprints = listOf(
            "C8:A2:E9:BC:CF:59:7C:2F:B6:DC:66:BE:E2:93:FC:13:F2:FC:47:EC:77:BC:6B:2B:0D:52:C1:1F:51:19:2A:B8",
            "A4:0D:A8:0A:59:D1:70:CA:A9:50:CF:15:C1:8C:45:4D:47:A3:9B:26:98:9D:8B:64:0E:CD:74:5B:A7:1B:F5:DC",
            "46:59:83:F7:79:1F:2A:BE:B4:3E:A2:CB:DC:7F:21:A8:26:0B:72:BC:08:A5:5C:83:9F:C1:A4:3B:C7:41:A8:1E",
            "28:BB:FE:4A:7B:97:E7:46:81:DC:55:C2:FB:B6:CC:B8:D6:C7:49:63:73:3F:6A:F6:AE:74:D8:C3:A6:E8:79:FD"
        )

        val json = allowlistFile().readText().uppercase()

        val offenders = publicTestKeyFingerprints.filter { fingerprint ->
            json.contains(fingerprint.uppercase())
        }

        assertTrue(
            offenders.isEmpty(),
            "Privileged allowlist contains public AOSP test key(s): $offenders. " +
                "These have publicly-distributed private keys and must be stripped from the " +
                "vendored snapshot (see ResourcePrivilegedBrowserAllowlistProvider)."
        )
    }

    @Test
    fun `fdroid flavour override is valid and additionally trusts fdroid re-signing keys`() {
        val json = fdroidOverrideFile().readText()
        val allowlist = Json { ignoreUnknownKeys = true }.decodeFromString<Allowlist>(json)

        val packages = allowlist.apps.associateBy({ it.info.packageName })
        assertTrue(packages.containsKey("com.duckduckgo.mobile.android"))
        assertTrue(packages.containsKey("org.mozilla.fennec_fdroid"))

        // The fdroid override must be a superset of the base snapshot.
        val baseJson = allowlistFile().readText()
        val base = Json { ignoreUnknownKeys = true }.decodeFromString<Allowlist>(baseJson)
        val basePackages = base.apps.map { it.info.packageName }.toSet()
        assertTrue(packages.keys.containsAll(basePackages))

        // F-Droid re-signing keys must NOT leak into the base snapshot used by other flavours.
        val baseText = baseJson.uppercase()
        val fdroidOnlyPackages = packages.keys - basePackages
        fdroidOnlyPackages.forEach { pkg ->
            assertTrue(!baseText.contains(pkg.uppercase()), "$pkg leaked into base allowlist")
        }
    }

    private fun allowlistFile(): File {
        val resourceFiles = listOf(
            "pass/browser-allowlist/impl/src/main/res/raw/passkey_privileged_browsers_allowlist.json",
            "src/main/res/raw/passkey_privileged_browsers_allowlist.json"
        )
        return resourceFiles.map(::File).first(File::exists)
    }

    private fun fdroidOverrideFile(): File {
        val resourceFiles = listOf(
            "app/src/fdroid/res/raw/passkey_privileged_browsers_allowlist.json",
            "../../../app/src/fdroid/res/raw/passkey_privileged_browsers_allowlist.json"
        )
        return resourceFiles.map(::File).first(File::exists)
    }
}
