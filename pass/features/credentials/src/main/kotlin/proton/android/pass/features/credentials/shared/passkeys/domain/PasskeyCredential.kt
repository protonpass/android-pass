/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.shared.passkeys.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import proton.android.pass.data.api.usecases.passkeys.PasskeySelection
import proton.android.pass.domain.PasskeyId

@[Serializable OptIn(ExperimentalSerializationApi::class)]
internal data class PasskeyCredential(
    @SerialName("rpId")
    internal val domain: String,
    @JsonNames("allowCredentials", "allowList")
    internal val allowedCredentials: List<PasskeyAllowedCredential>? = null
) {

    internal val passkeySelection: PasskeySelection by lazy {
        allowedCredentials
            ?.filter { allowedCredential -> allowedCredential.type == "public-key" }
            ?.map { allowedCredential -> PasskeyId(value = allowedCredential.id) }
            ?.let { allowedPasskeyIds ->
                if (allowedPasskeyIds.isEmpty()) PasskeySelection.All
                else PasskeySelection.Allowed(allowedPasskeyIds)
            }
            ?: PasskeySelection.All
    }

}
