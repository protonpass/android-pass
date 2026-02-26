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

package proton.android.pass.data.fakes.usecases

import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.User
import proton.android.pass.crypto.api.usecases.invites.AcceptGroupInvite
import proton.android.pass.crypto.api.usecases.invites.EncryptedGroupInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteKey

class FakeAcceptGroupInvite(
    private var result: List<EncryptedGroupInviteAcceptKey> = emptyList()
) : AcceptGroupInvite {

    var lastUser: User? = null
    var lastGroupKeys: List<PrivateAddressKey>? = null
    var lastUnlockedOrganizationKey: UnlockedKey? = null
    var lastInviterKeys: List<PublicKey>? = null
    var lastKeys: List<EncryptedInviteKey>? = null
    var lastIsGroupOwner: Boolean? = null

    override fun invoke(
        user: User,
        groupPrivateKeys: List<PrivateAddressKey>,
        unlockedOrganizationKey: UnlockedKey?,
        inviterAddressKeys: List<PublicKey>,
        keys: List<EncryptedInviteKey>,
        isGroupOwner: Boolean
    ): List<EncryptedGroupInviteAcceptKey> {
        lastUser = user
        lastGroupKeys = groupPrivateKeys
        lastUnlockedOrganizationKey = unlockedOrganizationKey
        lastInviterKeys = inviterAddressKeys
        lastKeys = keys
        lastIsGroupOwner = isGroupOwner
        return result
    }

    fun setResult(result: List<EncryptedGroupInviteAcceptKey>) {
        this.result = result
    }
}
