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

package proton.android.pass.features.vault.folders

import androidx.navigation.NavGraphBuilder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.dialog
import proton.android.pass.navigation.api.toPath

object AddFolderToVaultDialog : NavItem(
    baseRoute = "vault/addFolder/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId),
    optionalArgIds = listOf(CommonOptionalNavArgId.FolderId),
    navItemType = NavItemType.Dialog
) {
    // if folderId == null, it means it is the root folder (directly into the Vault)
    fun createNavRoute(shareId: ShareId, folderId: FolderId? = null): String = buildString {
        append("$baseRoute/${shareId.id}")

        folderId?.let { folderId ->
            mapOf(CommonOptionalNavArgId.FolderId.key to folderId.id)
                .toPath()
                .also(::append)
        }
    }
}

fun NavGraphBuilder.addFolderVaultDialogGraph(onNavigate: (VaultNavigation) -> Unit) {
    dialog(AddFolderToVaultDialog) {
        AddFolderToVaultDialog(
            onNavigate = onNavigate
        )
    }
}

