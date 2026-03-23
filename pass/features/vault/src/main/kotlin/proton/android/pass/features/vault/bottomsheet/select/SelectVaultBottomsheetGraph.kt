/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.vault.bottomsheet.select

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.bottomSheet

object SelectedVaultArg : NavArgId {
    override val key = "selectedVault"
    override val navType = NavType.StringType
}

object ShowFoldersArg : OptionalNavArgId {
    override val key = "showFolders"
    override val navType = NavType.BoolType
    override val default: Any = true
}

object SelectVaultBottomsheet : NavItem(
    baseRoute = "vault/select/bottomsheet",
    navArgIds = listOf(SelectedVaultArg),
    optionalArgIds = listOf(CommonOptionalNavArgId.FolderId, ShowFoldersArg),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(
        selectedVault: ShareId,
        selectedFolder: FolderId? = null,
        showFolders: Boolean = true
    ): String {
        val base = "$baseRoute/${selectedVault.id}"
        val params = buildList {
            if (selectedFolder != null) add("${CommonOptionalNavArgId.FolderId.key}=${selectedFolder.id}")
            if (!showFolders) add("${ShowFoldersArg.key}=false")
        }
        return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
    }
}

fun NavGraphBuilder.selectVaultBottomsheetGraph(onNavigate: (VaultNavigation) -> Unit) {
    bottomSheet(SelectVaultBottomsheet) {
        SelectVaultBottomsheet(
            onNavigate = onNavigate
        )
    }
}
