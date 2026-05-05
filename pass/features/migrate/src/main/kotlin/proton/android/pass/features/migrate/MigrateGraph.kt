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

package proton.android.pass.features.migrate

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.migrate.confirmvault.MigrateConfirmVaultBottomSheet
import proton.android.pass.features.migrate.warningshared.navigation.MigrateSharedWarningNavItem
import proton.android.pass.features.migrate.warningshared.ui.MigrateSharedWarningDialog
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.dialog
import proton.android.pass.navigation.api.toPath

sealed interface MigrateNavigation {

    data object Close : MigrateNavigation

    data class ItemMigrated(
        val shareId: ShareId,
        val itemId: ItemId
    ) : MigrateNavigation

    data object VaultMigrated : MigrateNavigation

    data object DismissBottomsheet : MigrateNavigation

    data object FolderMoved : MigrateNavigation

    @JvmInline
    value class VaultSelectionForVaultMigration(val shareId: ShareId) : MigrateNavigation

    data class VaultSelectionForItemsMigration(
        val filter: MigrateVaultFilter,
        val folderId: Option<FolderId> = None
    ) : MigrateNavigation

}

object MigrateModeArg : NavArgId {
    override val key: String = "migrateMode"
    override val navType: NavType<*> = NavType.StringType
}

enum class MigrateModeValue {
    SelectedItems,
    AllVaultItems,
    MoveFolder,
    MoveAllItemsInFolder
}

enum class MigrateVaultFilter {
    All,
    Shared
}

object MigrateVaultFilterArg : OptionalNavArgId {
    override val key = "migrateVaultFilter"
    override val navType = NavType.StringType
}

object MigrateConfirmVault : NavItem(
    baseRoute = "migrate/confirm",
    navArgIds = listOf(MigrateModeArg),
    optionalArgIds = listOf(
        CommonOptionalNavArgId.ShareId,
        CommonOptionalNavArgId.FolderId,
        MigrateVaultFilterArg
    ),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRouteForMigrateAll(shareId: ShareId) = buildString {
        append("$baseRoute/${MigrateModeValue.AllVaultItems.name}")
        append(mapOf(CommonOptionalNavArgId.ShareId.key to shareId.id).toPath())
    }

    fun createNavRouteForMigrateSelectedItems(filter: MigrateVaultFilter, folderId: Option<FolderId> = None): String =
        buildString {
            append("$baseRoute/${MigrateModeValue.SelectedItems.name}")
            val map = mutableMapOf<String, Any>(MigrateVaultFilterArg.key to filter.name)
            if (folderId is Some) map[CommonOptionalNavArgId.FolderId.key] = folderId.value.id
            append(map.toPath())
        }

    fun createNavRouteForMoveFolder(shareId: ShareId, folderId: FolderId) = buildString {
        append("$baseRoute/${MigrateModeValue.MoveFolder.name}")
        append(
            mapOf(
                CommonOptionalNavArgId.ShareId.key to shareId.id,
                CommonOptionalNavArgId.FolderId.key to folderId.id
            ).toPath()
        )
    }

    fun createNavRouteForMoveAllItemsInFolder(shareId: ShareId, folderId: FolderId) = buildString {
        append("$baseRoute/${MigrateModeValue.MoveAllItemsInFolder.name}")
        append(
            mapOf(
                CommonOptionalNavArgId.ShareId.key to shareId.id,
                CommonOptionalNavArgId.FolderId.key to folderId.id
            ).toPath()
        )
    }
}

fun NavGraphBuilder.migrateGraph(navigation: (MigrateNavigation) -> Unit) {
    bottomSheet(MigrateConfirmVault) {
        MigrateConfirmVaultBottomSheet(
            navigation = navigation
        )
    }

    dialog(MigrateSharedWarningNavItem) {
        MigrateSharedWarningDialog(onNavigate = navigation)
    }
}
