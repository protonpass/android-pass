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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal class PasskeyActivityOriginResolver @Inject constructor(
    private val passkeyOriginVerifier: PasskeyOriginVerifier
) {

    internal suspend fun resolve(callingAppInfo: CallingAppInfo, requestJson: String): String? {
        val rpId = extractRpIdFromJson(requestJson) ?: run {
            PassLogger.w(TAG, "Rejecting: unable to extract rpId from requestJson")
            return null
        }
        return passkeyOriginVerifier.verifyOrigin(
            callingAppInfo = callingAppInfo,
            requestedRpId = rpId
        )
    }

    private companion object {

        private const val TAG = "PasskeyActivityOriginResolver"

        private fun extractRpIdFromJson(requestJson: String): String? = runCatching {
            Json.parseToJsonElement(requestJson).jsonObject["rpId"]?.jsonPrimitive?.content
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }
}
