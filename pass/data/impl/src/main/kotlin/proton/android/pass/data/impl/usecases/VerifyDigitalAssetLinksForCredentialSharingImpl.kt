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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import proton.android.pass.common.api.safeRunCatching
import okhttp3.OkHttpClient
import okhttp3.Request
import proton.android.pass.data.api.usecases.VerifyDigitalAssetLinksForCredentialSharing
import proton.android.pass.data.impl.remote.PublicOkhttpClient
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class VerifyDigitalAssetLinksForCredentialSharingImpl @Inject constructor(
    @param:PublicOkhttpClient private val okHttpClient: OkHttpClient
) : VerifyDigitalAssetLinksForCredentialSharing {

    override suspend fun invoke(
        website: String,
        packageName: String,
        certificateFingerprints: Set<String>
    ): Boolean = withContext(Dispatchers.IO) {
        safeRunCatching {
            val normalizedFingerprints = certificateFingerprints
                .map(::normalizeFingerprint)
                .toSet()
            if (normalizedFingerprints.isEmpty()) return@safeRunCatching false

            val request = Request.Builder()
                .url("${website.trimEnd('/')}/.well-known/assetlinks.json")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@safeRunCatching false
                val body = response.body ?: return@safeRunCatching false
                if (body.contentLength() > MAX_RESPONSE_SIZE_BYTES) return@safeRunCatching false
                val source = body.source()
                source.request(MAX_RESPONSE_SIZE_BYTES + 1L)
                if (source.buffer.size > MAX_RESPONSE_SIZE_BYTES) return@safeRunCatching false
                val bodyString = source.buffer.readUtf8()
                val assetLinks = jsonParser.parseToJsonElement(bodyString).jsonArray
                assetLinks.any { element ->
                    isMatchingAssetLink(
                        link = element.jsonObject,
                        packageName = packageName,
                        normalizedFingerprints = normalizedFingerprints
                    )
                }
            }
        }.getOrElse { error ->
            PassLogger.w(TAG, "Failed to validate native app Digital Asset Links")
            PassLogger.w(TAG, error)
            false
        }
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
            normalizeFingerprint(element.jsonPrimitive.content) in normalizedFingerprints
        }
    }

    private companion object {
        private const val TAG = "VerifyDigitalAssetLinks"
        private const val MAX_RESPONSE_SIZE_BYTES = 2 * 1024 * 1024
        private const val RELATION_GET_LOGIN_CREDS = "delegate_permission/common.get_login_creds"

        private val jsonParser = Json { ignoreUnknownKeys = true }

        private fun normalizeFingerprint(value: String): String = value.replace(":", "").lowercase()
    }
}
