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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.provider.CallingAppInfo
import proton.android.pass.crypto.api.Base64
import proton.android.pass.data.api.usecases.VerifyDigitalAssetLinksForCredentialSharing
import proton.android.pass.log.api.PassLogger
import java.security.MessageDigest
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal class PasskeyOriginVerifier @Inject constructor(
    private val verifyDigitalAssetLinksForCredentialSharing: VerifyDigitalAssetLinksForCredentialSharing,
    private val privilegedBrowserAllowlistProvider: PrivilegedBrowserAllowlistProvider
) {

    internal suspend fun verifyOrigin(callingAppInfo: CallingAppInfo?, requestedRpId: String): String? {
        if (!isValidRpId(requestedRpId)) {
            PassLogger.w(TAG, "Rejecting passkey request: rpId is not a valid hostname")
            return null
        }

        if (callingAppInfo == null) {
            PassLogger.w(TAG, "Rejecting passkey request: no caller identity available")
            return null
        }

        if (callingAppInfo.isOriginPopulated()) {
            val browserOrigin = getBrowserOrigin(callingAppInfo) ?: run {
                PassLogger.w(TAG, "Rejecting passkey request: privileged caller not in allowlist")
                return null
            }
            return verifyBrowserOrigin(browserOrigin, requestedRpId)
        }

        return verifyNativeAppOrigin(callingAppInfo, requestedRpId)
    }

    private fun getBrowserOrigin(callingAppInfo: CallingAppInfo): String? = runCatching {
        callingAppInfo.getOrigin(privilegedBrowserAllowlistProvider.json)
    }.getOrElse {
        null
    }

    private fun verifyBrowserOrigin(browserOrigin: String, requestedRpId: String): String? {
        val originHost = extractHost(browserOrigin)
        if (originHost == null || !isDomainMatch(originHost, requestedRpId)) {
            PassLogger.w(TAG, "Rejecting passkey request: browser origin does not match requested rpId")
            return null
        }
        return browserOrigin
    }

    private suspend fun verifyNativeAppOrigin(callingAppInfo: CallingAppInfo, requestedRpId: String): String? {
        val packageName = callingAppInfo.packageName

        val signingKeyHash = getSigningKeyHash(callingAppInfo)
        if (signingKeyHash == null) {
            PassLogger.w(TAG, "Rejecting passkey request: unable to determine caller signing key")
            return null
        }

        val certificateFingerprints = getSigningCertificateFingerprints(callingAppInfo)
        val isAuthorized = verifyDigitalAssetLinksForCredentialSharing(
            website = "https://$requestedRpId",
            packageName = packageName,
            certificateFingerprints = certificateFingerprints
        )
        if (!isAuthorized) {
            PassLogger.w(TAG, "Rejecting passkey request: native app not authorized via live Digital Asset Links")
            return null
        }

        return "https://$requestedRpId"
    }

    companion object {

        private const val TAG = "PasskeyOriginVerifier"

        // Per WebAuthn spec, rpId must be a valid hostname: ASCII letters, digits, dots, hyphens.
        // No scheme, no userinfo, no port, no path — prevents URL injection into DAL fetches.
        private val rpIdRegex = Regex("^[a-zA-Z0-9]([a-zA-Z0-9._-]*[a-zA-Z0-9])?$")

        internal fun isValidRpId(rpId: String): Boolean = rpIdRegex.matches(rpId)

        internal fun getSigningKeyHash(callingAppInfo: CallingAppInfo): String? {
            val signingInfo = callingAppInfo.signingInfo
            val signers = signingInfo.apkContentsSigners.map { signer -> signer.toByteArray() }
            return getSigningKeyHash(
                signingCertificateBytes = signers,
                hasMultipleSigners = signingInfo.hasMultipleSigners()
            )
        }

        internal fun getSigningKeyHash(signingCertificateBytes: List<ByteArray>, hasMultipleSigners: Boolean): String? {
            if (hasMultipleSigners) return null
            val certBytes = signingCertificateBytes.singleOrNull() ?: return null
            val digest = MessageDigest.getInstance("SHA-256").digest(certBytes)
            return Base64.encodeBase64String(digest, Base64.Mode.UrlSafe)
        }

        internal fun getSigningCertificateFingerprints(callingAppInfo: CallingAppInfo): Set<String> {
            val signingInfo = callingAppInfo.signingInfo
            val signers = if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners.toList()
            } else {
                signingInfo.signingCertificateHistory?.toList()
                    ?: signingInfo.apkContentsSigners.toList()
            }

            val md = MessageDigest.getInstance("SHA-256")
            return signers.map { signer ->
                md.reset()
                val digest = md.digest(signer.toByteArray())
                digest.joinToString(":") { byte -> "%02X".format(byte) }
            }.toSet()
        }

        internal fun extractHost(origin: String): String? = runCatching {
            java.net.URI(origin).host
        }.getOrNull()

        internal fun isDomainMatch(host: String, rpId: String): Boolean {
            if (host.equals(rpId, ignoreCase = true)) return true
            return host.endsWith(".$rpId", ignoreCase = true)
        }
    }
}
