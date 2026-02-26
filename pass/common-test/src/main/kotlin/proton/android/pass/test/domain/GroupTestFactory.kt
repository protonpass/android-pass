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

package proton.android.pass.test.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import proton.android.pass.domain.Group
import proton.android.pass.domain.GroupAddress
import proton.android.pass.domain.GroupId

object GroupTestFactory {
    fun create(
        id: GroupId = GroupId("group-id"),
        email: String = "group@test.com",
        includeKeys: Boolean = true
    ): Group = Group(
        id = id,
        name = "group",
        address = GroupAddress(
            id = "address-id",
            email = email,
            keys = if (includeKeys) listOf(defaultPrivateAddressKey()) else emptyList()
        ),
        permissions = 0,
        createTime = 0,
        flags = 0,
        groupVisibility = 0,
        memberVisibility = 0,
        description = null
    )

    private fun defaultPrivateAddressKey(): PrivateAddressKey = PrivateAddressKey(
        addressId = "address-id",
        privateKey = PrivateKey(
            key = "priv-key",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = EncryptedByteArray(byteArrayOf())
        ),
        token = "token",
        signature = null
    )
}
