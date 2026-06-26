/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.passkeys.impl

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import proton.android.pass.common.api.toLogToken
import proton.android.pass.commonrust.AuthenticateWithPasskeyAndroidRequest
import proton.android.pass.commonrust.PasskeyManager
import proton.android.pass.domain.Passkey
import proton.android.pass.log.api.PassLogger
import proton.android.pass.passkeys.api.AuthenticateWithPasskey
import proton.android.pass.passkeys.api.PasskeyAuthenticationResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticateWithPasskeyImpl @Inject constructor(
    private val passkeyManager: PasskeyManager
) : AuthenticateWithPasskey {

    override fun invoke(
        origin: String,
        passkey: Passkey,
        requestJson: String,
        clientDataHash: ByteArray?
    ): PasskeyAuthenticationResponse {
        val sanitized = PasskeyJsonSanitizer.sanitize(requestJson)
        PassLogger.i(TAG, "Resolving challenge")

        runCatching {
            val request = AuthenticateWithPasskeyAndroidRequest(
                origin = origin,
                request = sanitized,
                passkey = passkey.contents.data,
                clientDataHash = clientDataHash
            )
            passkeyManager.resolveChallengeForAndroid(request)
        }.fold(
            onSuccess = {
                PassLogger.i(TAG, "Challenge resolved successfully")
                return PasskeyAuthenticationResponse(it)
            },
            onFailure = {
                val rpToken = extractRpId(requestJson)?.toLogToken() ?: "unknown"
                val allowedCount = extractAllowedCredentialsCount(requestJson)
                PassLogger.w(
                    TAG,
                    "Challenge resolution failed: ${it::class.simpleName} " +
                        "rpId=$rpToken allowedCreds=$allowedCount"
                )
                throw it
            }
        )
    }

    companion object {
        private const val TAG = "AuthenticateWithPasskeyImpl"

        private fun extractRpId(requestJson: String): String? = runCatching {
            Json.parseToJsonElement(requestJson).jsonObject["rpId"]?.jsonPrimitive?.content
        }.getOrNull()

        private fun extractAllowedCredentialsCount(requestJson: String): Int = runCatching {
            Json.parseToJsonElement(requestJson).jsonObject["allowCredentials"]?.jsonArray?.size ?: 0
        }.getOrElse { 0 }
    }

}
