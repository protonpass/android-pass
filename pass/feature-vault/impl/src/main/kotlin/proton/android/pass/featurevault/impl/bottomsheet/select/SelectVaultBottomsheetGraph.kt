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

package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.pass.domain.ShareId

object SelectedVaultArg : NavArgId {
    override val key = "selectedVault"
    override val navType = NavType.StringType
}

object SelectVaultBottomsheet : NavItem(
    baseRoute = "vault/select/bottomsheet",
    navArgIds = listOf(SelectedVaultArg),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(selectedVault: ShareId) = "$baseRoute/${selectedVault.id}"
}

fun NavGraphBuilder.selectVaultBottomsheetGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    bottomSheet(SelectVaultBottomsheet) {
        SelectVaultBottomsheet(
            onNavigate = onNavigate
        )
    }
}
