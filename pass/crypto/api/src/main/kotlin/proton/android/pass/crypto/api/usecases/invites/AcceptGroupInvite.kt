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

package proton.android.pass.crypto.api.usecases.invites

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey

data class EncryptedGroupInviteAcceptKey(
    val keyRotation: Long,
    val key: String,
    val localEncryptedKey: EncryptedByteArray
)

interface AcceptGroupInvite {
    operator fun invoke(
        groupPrivateKeys: List<PrivateAddressKey>,
        organizationPrivateKey: PrivateKey,
        inviterAddressKeys: List<PublicKey>,
        keys: List<EncryptedInviteKey>
    ): List<EncryptedGroupInviteAcceptKey>
}
