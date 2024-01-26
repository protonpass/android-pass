/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.usecases.capabilities

import proton.android.pass.data.api.usecases.capabilities.CanManageVaultAccess
import proton.android.pass.data.api.usecases.capabilities.VaultAccessData
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.Vault
import proton.android.pass.domain.hasFlag
import proton.android.pass.domain.toPermissions
import javax.inject.Inject

class CanManageVaultAccessImpl @Inject constructor() : CanManageVaultAccess {
    override suspend fun invoke(vault: Vault): VaultAccessData {
        val isAdmin = vault.role.toPermissions().hasFlag(SharePermissionFlag.Admin)
        return VaultAccessData(
            canManageAccess = isAdmin,
            canViewMembers = !isAdmin
        )
    }
}
