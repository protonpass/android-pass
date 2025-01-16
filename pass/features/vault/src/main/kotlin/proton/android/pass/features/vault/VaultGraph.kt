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

package proton.android.pass.features.vault

import androidx.navigation.NavGraphBuilder
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.bottomsheet.createVaultGraph
import proton.android.pass.features.vault.bottomsheet.editVaultGraph
import proton.android.pass.features.vault.bottomsheet.options.bottomSheetVaultOptionsGraph
import proton.android.pass.features.vault.bottomsheet.select.selectVaultBottomsheetGraph
import proton.android.pass.features.vault.delete.deleteVaultDialogGraph
import proton.android.pass.features.vault.leave.leaveVaultDialogGraph

sealed interface VaultNavigation {
    data object Upgrade : VaultNavigation
    data object CloseScreen : VaultNavigation
    data object DismissBottomsheet : VaultNavigation

    @JvmInline
    value class VaultSelected(val shareId: ShareId) : VaultNavigation

    @JvmInline
    value class VaultMigrate(val shareId: ShareId) : VaultNavigation

    @JvmInline
    value class VaultEdit(val shareId: ShareId) : VaultNavigation

    @JvmInline
    value class VaultRemove(val shareId: ShareId) : VaultNavigation

    data class VaultShare(
        val shareId: ShareId,
        val showEditVault: Boolean
    ) : VaultNavigation

    @JvmInline
    value class VaultLeave(val shareId: ShareId) : VaultNavigation

    @JvmInline
    value class VaultAccess(val shareId: ShareId) : VaultNavigation
}

fun NavGraphBuilder.vaultGraph(onNavigate: (VaultNavigation) -> Unit) {
    createVaultGraph(onNavigate)
    editVaultGraph(onNavigate)
    deleteVaultDialogGraph(onNavigate)
    leaveVaultDialogGraph(onNavigate)
    selectVaultBottomsheetGraph(onNavigate)
    bottomSheetVaultOptionsGraph(onNavigate)
}
