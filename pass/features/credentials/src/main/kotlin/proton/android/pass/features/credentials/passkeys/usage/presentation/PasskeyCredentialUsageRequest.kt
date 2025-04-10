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

package proton.android.pass.features.credentials.passkeys.usage.presentation

import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId

internal data class PasskeyCredentialUsageRequest(
    internal val requestJson: String,
    internal val requestOrigin: String,
    internal val clientDataHash: ByteArray?,
    internal val shareId: ShareId,
    internal val itemId: ItemId,
    internal val passkeyId: PasskeyId
) {

    @Suppress("ReturnCount")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PasskeyCredentialUsageRequest

        if (requestJson != other.requestJson) return false
        if (requestOrigin != other.requestOrigin) return false
        if (!clientDataHash.contentEquals(other.clientDataHash)) return false
        if (shareId != other.shareId) return false
        if (itemId != other.itemId) return false
        if (passkeyId != other.passkeyId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestJson.hashCode()
        result = 31 * result + requestOrigin.hashCode()
        result = 31 * result + (clientDataHash?.contentHashCode() ?: 0)
        result = 31 * result + shareId.hashCode()
        result = 31 * result + itemId.hashCode()
        result = 31 * result + passkeyId.hashCode()
        return result
    }

}
