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

import java.io.IOException
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.net.ssl.SSLHandshakeException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.toLogToken
import okhttp3.OkHttpClient
import okhttp3.Request
import proton.android.pass.data.api.usecases.VerifyDigitalAssetLinksForCredentialSharing
import proton.android.pass.data.api.AssetLinkOkHttpClient
import proton.android.pass.data.impl.AppCertificate
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkRestrictionDiagnostics
import javax.inject.Inject

class VerifyDigitalAssetLinksForCredentialSharingImpl @Inject constructor(
    @param:AssetLinkOkHttpClient private val okHttpClient: OkHttpClient,
    private val networkRestrictionDiagnostics: NetworkRestrictionDiagnostics,
    private val appDispatchers: AppDispatchers
) : VerifyDigitalAssetLinksForCredentialSharing {

    override suspend fun invoke(
        website: String,
        packageName: String,
        certificateFingerprints: Set<String>
    ): Boolean = withContext(appDispatchers.io) {
        safeRunCatching {
            checkDigitalAssetLinks(website, packageName, certificateFingerprints)
        }.getOrElse { error ->
            logFailure(error)
            false
        }
    }

    private fun checkDigitalAssetLinks(
        website: String,
        packageName: String,
        certificateFingerprints: Set<String>
    ): Boolean {
        val normalizedFingerprints = certificateFingerprints
            .map(AppCertificate::normalizeSha256Fingerprint)
            .toSet()
        val siteToken = website.toLogToken()
        val pkgToken = packageName.toLogToken()
        if (normalizedFingerprints.isEmpty()) {
            PassLogger.w(TAG, "DAL check skipped: no certificate fingerprints pkg=$pkgToken site=$siteToken")
            return false
        }

        PassLogger.i(TAG, "DAL check: fetching assetlinks.json site=$siteToken pkg=$pkgToken")
        val request = Request.Builder()
            .url("${website.trimEnd('/')}/.well-known/assetlinks.json")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            return matchAssetLinksResponse(
                response = response,
                packageName = packageName,
                normalizedFingerprints = normalizedFingerprints,
                siteToken = siteToken,
                pkgToken = pkgToken
            )
        }
    }

    private fun matchAssetLinksResponse(
        response: Response,
        packageName: String,
        normalizedFingerprints: Set<String>,
        siteToken: String,
        pkgToken: String
    ): Boolean {
        if (!response.isSuccessful) {
            PassLogger.w(TAG, "DAL check failed: HTTP ${response.code} site=$siteToken pkg=$pkgToken")
            return false
        }
        val body = response.body ?: return false
        if (body.contentLength() > MAX_RESPONSE_SIZE_BYTES) return false
        val source = body.source()
        source.request(MAX_RESPONSE_SIZE_BYTES + 1L)
        if (source.buffer.size > MAX_RESPONSE_SIZE_BYTES) return false
        val bodyString = source.buffer.readUtf8()
        val assetLinks = jsonParser.parseToJsonElement(bodyString).jsonArray
        return assetLinks.any { element ->
            isMatchingAssetLink(
                link = element.jsonObject,
                packageName = packageName,
                normalizedFingerprints = normalizedFingerprints
            )
        }.also { matched ->
            PassLogger.i(
                TAG,
                "DAL check: match=$matched entries=${assetLinks.size} site=$siteToken pkg=$pkgToken"
            )
        }
    }

    private fun logFailure(error: Throwable) {
        when (error) {
            is SSLHandshakeException -> PassLogger.w(
                TAG,
                "TLS handshake failed during DAL verification, possible MITM or server misconfiguration"
            )

            is IOException -> logNetworkFailure()

            else -> PassLogger.w(TAG, "Failed to validate Digital Asset Links")
        }
        PassLogger.w(TAG, error)
    }

    private fun logNetworkFailure() {
        val snapshot = networkRestrictionDiagnostics.snapshot()
        val likelyCause = if (snapshot.isActiveNetworkMetered && snapshot.isBackgroundDataRestricted) {
            " (likely cause: Data Saver is blocking background network" +
                " access for this app on the current metered connection)"
        } else {
            ""
        }
        PassLogger.w(
            TAG,
            "Network request failed during DAL verification: " +
                "isActiveNetworkMetered=${snapshot.isActiveNetworkMetered} " +
                "isBackgroundDataRestricted=${snapshot.isBackgroundDataRestricted}$likelyCause"
        )
    }

    private fun isMatchingAssetLink(
        link: JsonObject,
        packageName: String,
        normalizedFingerprints: Set<String>
    ): Boolean {
        val relation = link["relation"]?.jsonArray ?: return false
        val target = link["target"]?.jsonObject ?: return false

        if (target["namespace"]?.jsonPrimitive?.content != "android_app") return false
        if (target["package_name"]?.jsonPrimitive?.content != packageName) return false

        val hasGetLoginCreds = relation.any { it.jsonPrimitive.content == RELATION_GET_LOGIN_CREDS }
        if (!hasGetLoginCreds) return false

        val fingerprints = target["sha256_cert_fingerprints"]?.jsonArray ?: return false
        return fingerprints.any { element ->
            AppCertificate.normalizeSha256Fingerprint(element.jsonPrimitive.content) in normalizedFingerprints
        }
    }

    private companion object {
        private const val TAG = "VerifyDigitalAssetLinks"
        private const val MAX_RESPONSE_SIZE_BYTES = 2 * 1024 * 1024
        private const val RELATION_GET_LOGIN_CREDS = "delegate_permission/common.get_login_creds"

        private val jsonParser = Json { ignoreUnknownKeys = true }
    }
}
