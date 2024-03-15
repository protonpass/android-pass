/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featurepasskeys.select

import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import androidx.credentials.provider.ProviderGetCredentialRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import proton.android.pass.data.api.usecases.passkeys.PasskeySelection
import proton.android.pass.domain.PasskeyId
import proton.android.pass.log.api.PassLogger

object SelectPasskeyUtils {

    private const val TAG = "SelectPasskeyUtils"

    private val jsonParser = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class CredentialRequest(
        val rpId: String,
        val allowCredentials: List<AllowedCredential>
    )

    @Serializable
    private data class AllowedCredential(
        val type: String,
        val id: String
    )

    data class PasskeyFilterParameters(
        val domain: String?,
        val passkeySelection: PasskeySelection
    )

    fun getPasskeyFilterParameters(request: BeginGetCredentialRequest): PasskeyFilterParameters {
        val domain = getDomainFromRequest(request)
        val allowedPasskeys = getAllowedPasskeysFromRequest(request)
        return PasskeyFilterParameters(domain, allowedPasskeys)
    }

    fun getDomainFromRequest(request: ProviderGetCredentialRequest): String? {
        val origin = request.callingAppInfo.origin
        if (origin != null) return origin

        val json = request.credentialOptions
            .firstOrNull { it is GetPublicKeyCredentialOption }
            ?.let { it as GetPublicKeyCredentialOption }
            ?.requestJson
            ?: return null

        return parseRequest(json)?.rpId
    }

    private fun getDomainFromRequest(request: BeginGetCredentialRequest): String? {
        val origin = request.callingAppInfo?.origin
        if (origin != null) return origin

        val json = request.beginGetCredentialOptions
            .firstOrNull { it is BeginGetPublicKeyCredentialOption }
            ?.let { it as BeginGetPublicKeyCredentialOption }
            ?.requestJson
            ?: return null

        return parseRequest(json)?.rpId
    }


    private fun getAllowedPasskeysFromRequest(request: BeginGetCredentialRequest): PasskeySelection {
        val json = request.beginGetCredentialOptions
            .firstOrNull { it is BeginGetPublicKeyCredentialOption }
            ?.let { it as BeginGetPublicKeyCredentialOption }
            ?.requestJson
            ?: return PasskeySelection.All

        val parsed = parseRequest(json) ?: return PasskeySelection.All
        val allowed = parsed.allowCredentials
            .filter { it.type == "public-key" }
            .map { PasskeyId(it.id) }

        return if (allowed.isEmpty()) {
            PasskeySelection.All
        } else {
            PasskeySelection.Allowed(allowed)
        }
    }

    private fun parseRequest(requestJson: String): CredentialRequest? = runCatching {
        jsonParser.decodeFromString<CredentialRequest>(requestJson)
    }.getOrElse {
        PassLogger.w(TAG, "Error parsing JSON request")
        PassLogger.w(TAG, it)
        null
    }

}
