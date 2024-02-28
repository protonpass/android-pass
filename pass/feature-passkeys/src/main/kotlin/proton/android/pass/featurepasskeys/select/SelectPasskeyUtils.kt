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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object SelectPasskeyUtils {

    fun getDomainFromRequest(request: BeginGetCredentialRequest): String? {
        val origin = request.callingAppInfo?.origin
        if (origin != null) return origin

        val json = request.beginGetCredentialOptions
            .firstOrNull { it is BeginGetPublicKeyCredentialOption }
            ?.let { it as BeginGetPublicKeyCredentialOption }
            ?.requestJson
            ?: return null

        return getDomainFromRequest(json)
    }

    fun getDomainFromRequest(request: ProviderGetCredentialRequest): String? {
        val origin = request.callingAppInfo.origin
        if (origin != null) return origin

        val json = request.credentialOptions
            .firstOrNull { it is GetPublicKeyCredentialOption }
            ?.let { it as GetPublicKeyCredentialOption }
            ?.requestJson
            ?: return null

        return getDomainFromRequest(json)
    }

    private fun getDomainFromRequest(requestJson: String): String? = runCatching {
        val data = Json.parseToJsonElement(requestJson)
        data.jsonObject["rpId"]?.jsonPrimitive?.content
    }.getOrNull()
}
