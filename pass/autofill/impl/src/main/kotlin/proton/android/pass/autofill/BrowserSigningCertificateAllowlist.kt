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

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import proton.android.pass.browserallowlist.api.PrivilegedBrowserAllowlistProvider

/**
 * Fingerprint lookup built from the single vendored allowlist shared with the passkey
 * feature ([PrivilegedBrowserAllowlistProvider]), instead of a second, separately-vendored
 * copy — one file to keep in sync as browsers rotate signing keys.
 *
 * Only "release"-signed certificates are trusted here: unlike the passkey origin-verification
 * use case, this lookup gates a security-relevant "is this really that browser" decision, and a
 * userdebug/test signing key's private-key provenance can't be assumed non-public.
 */
internal object BrowserSigningCertificateAllowlist {

    private const val RELEASE_BUILD = "release"

    @Volatile
    private var cached: Map<String, Set<String>>? = null

    internal fun get(context: Context, packageName: String): Set<String> {
        val allowlist = cached ?: run {
            val provider = EntryPointAccessors.fromApplication(
                context.applicationContext,
                BrowserAllowlistEntryPoint::class.java
            ).privilegedBrowserAllowlistProvider()
            parse(provider.json).also { cached = it }
        }
        return allowlist[packageName].orEmpty()
    }

    internal fun parse(json: String): Map<String, Set<String>> = runCatching {
        Json { ignoreUnknownKeys = true }
            .decodeFromString<AllowlistRoot>(json)
            .apps
            .groupBy(
                keySelector = { app -> app.info.packageName },
                valueTransform = { app ->
                    app.info.signatures
                        .filter { signature -> signature.build == RELEASE_BUILD }
                        .map { signature -> signature.certFingerprintSha256 }
                }
            )
            .mapValues { (_, fingerprintLists) -> fingerprintLists.flatten().toSet() }
            .filterValues { fingerprints -> fingerprints.isNotEmpty() }
    }.getOrDefault(emptyMap())

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface BrowserAllowlistEntryPoint {
        fun privilegedBrowserAllowlistProvider(): PrivilegedBrowserAllowlistProvider
    }

    @Serializable
    private data class AllowlistRoot(val apps: List<AllowlistApp>)

    @Serializable
    private data class AllowlistApp(val info: AllowlistAppInfo)

    @Serializable
    private data class AllowlistAppInfo(
        @SerialName("package_name") val packageName: String,
        val signatures: List<AllowlistSignature>
    )

    @Serializable
    private data class AllowlistSignature(
        val build: String? = null,
        @SerialName("cert_fingerprint_sha256") val certFingerprintSha256: String
    )
}
