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

package proton.android.pass.featurevault.impl.delete

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.dialog
import proton.pass.domain.ShareId

object DeleteVaultDialog : NavItem(
    baseRoute = "vault/delete/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId),
    navItemType = NavItemType.Dialog
) {
    fun createNavRoute(shareId: ShareId): String = "$baseRoute/${shareId.id}"
}

fun NavGraphBuilder.deleteVaultDialogGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    dialog(DeleteVaultDialog) {
        DeleteVaultDialog(
            onNavigate = onNavigate
        )
    }
}

