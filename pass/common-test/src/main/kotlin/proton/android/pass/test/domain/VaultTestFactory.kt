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

package proton.android.pass.test.domain

import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import java.util.Date

object VaultTestFactory {

    fun create(
        shareId: ShareId = ShareId("123"),
        vaultId: VaultId = VaultId("456"),
        userId: UserId = UserId("789"),
        name: String = "Vault ${vaultId.id}",
        isOwned: Boolean = true,
        role: ShareRole = ShareRole.Admin,
        shared: Boolean = false,
        members: Int = 1,
        canAutofill: Boolean = true,
        createTime: Date = Date()
    ): Vault {
        return Vault(
            userId = userId,
            shareId = shareId,
            vaultId = vaultId,
            name = name,
            isOwned = isOwned,
            role = role,
            createTime = createTime,
            shared = shared,
            members = members,
            canAutofill = canAutofill,
            shareFlags = ShareFlags(0)
        )
    }
}
