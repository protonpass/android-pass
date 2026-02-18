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
import androidx.navigation.NavType
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.dialog
import proton.android.pass.navigation.api.toPath

object ParentFolderIdNavArgId : OptionalNavArgId {
    override val key: String = "parentFolderId"
    override val navType: NavType<*> = NavType.StringType
}

object AddFolderToVaultDialog : NavItem(
    baseRoute = "vault/addFolder/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId),
    optionalArgIds = listOf(ParentFolderIdNavArgId),
    navItemType = NavItemType.Dialog
) {
    fun createNavRoute(shareId: ShareId, parentFolderId: FolderId? = null): String = buildString {
        append("$baseRoute/${shareId.id}")

        parentFolderId?.let { parentFolderId ->
            mapOf(ParentFolderIdNavArgId.key to parentFolderId.id)
                .toPath()
                .also(::append)
        }
    }
}

object RenameFolderDialog : NavItem(
    baseRoute = "vault/renameFolder/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId),
    optionalArgIds = listOf(CommonOptionalNavArgId.FolderId),
    navItemType = NavItemType.Dialog
) {
    fun createNavRoute(shareId: ShareId, folderId: FolderId): String = buildString {
        append("$baseRoute/${shareId.id}")
        mapOf(CommonOptionalNavArgId.FolderId.key to folderId.id)
            .toPath()
            .also(::append)
    }
}

fun NavGraphBuilder.addFolderVaultDialogGraph(onNavigate: (VaultNavigation) -> Unit) {
    dialog(AddFolderToVaultDialog) {
        AddFolderToVaultDialog(
            onNavigate = onNavigate
        )
    }
}

fun NavGraphBuilder.renameFolderVaultDialogGraph(onNavigate: (VaultNavigation) -> Unit) {
    dialog(RenameFolderDialog) {
        AddFolderToVaultDialog(
            onNavigate = onNavigate
        )
    }
}

