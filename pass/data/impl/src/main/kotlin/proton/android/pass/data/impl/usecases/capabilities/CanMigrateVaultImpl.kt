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

import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanMigrateVault
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.hasFlag
import proton.android.pass.domain.toPermissions
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class CanMigrateVaultImpl @Inject constructor(
    private val observeVaults: ObserveVaults,
    private val canPerformPaidAction: CanPerformPaidAction
) : CanMigrateVault {

    /**
     * User can only migrate a vault if:
     * - They have at least one other vault
     * - They have delete permission on the vault (as migrate performs a create on target and delete on source)
     * - Paid account status:
     *   - If paid, they can migrate any vault
     *   - If not paid, they can migrate any vault they have write access to
     */
    @Suppress("ReturnCount")
    override suspend fun invoke(shareId: ShareId): Boolean {
        val vaults = observeVaults(includeHidden = true).firstOrNull() ?: return false
        if (vaults.isEmpty()) {
            PassLogger.w(TAG, "There are no vaults")
            return false
        }
        if (vaults.size < 2) return false

        val vault = vaults.firstOrNull { it.shareId == shareId } ?: return false
        val hasAnyOtherVaultWriteAccess = vaults.any {
            it.shareId != shareId && it.role.toPermissions().canCreate()
        }

        val hasDeletePermission = vault.role.toPermissions().hasFlag(SharePermissionFlag.Delete)
        if (!hasDeletePermission) return false

        val canDoPaidAction = canPerformPaidAction().firstOrNull() ?: false
        return if (canDoPaidAction) {
            true
        } else {
            hasAnyOtherVaultWriteAccess
        }
    }

    companion object {
        private const val TAG = "CanMigrateVaultImpl"
    }
}
