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
import proton.android.pass.features.migrate.selectvault.MigrateSelectVaultBottomSheet
import proton.android.pass.features.migrate.warningshared.navigation.MigrateSharedWarningNavItem
import proton.android.pass.features.migrate.warningshared.ui.MigrateSharedWarningDialog
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.dialog
import proton.android.pass.navigation.api.toPath

sealed interface MigrateNavigation {

    data object Close : MigrateNavigation

    data class VaultSelectedForMigrateItem(
        val destShareId: ShareId,
        val folderId: Option<FolderId> = None
    ) : MigrateNavigation

    data class VaultSelectedForMigrateAll(
        val sourceShareId: ShareId,
        val destShareId: ShareId
    ) : MigrateNavigation

    data class ItemMigrated(
        val shareId: ShareId,
        val itemId: ItemId
    ) : MigrateNavigation

    data object VaultMigrated : MigrateNavigation

    data object DismissBottomsheet : MigrateNavigation

    data object FolderMoved : MigrateNavigation

    data class VaultSelectedForMoveFolder(
        val shareId: ShareId,
        val folderId: FolderId,
        val newParentFolderId: FolderId? = null
    ) : MigrateNavigation

    @JvmInline
    value class VaultSelectionForVaultMigration(val shareId: ShareId) : MigrateNavigation

    @JvmInline
    value class VaultSelectionForItemsMigration(val filter: MigrateVaultFilter) : MigrateNavigation

}

object MigrateModeArg : NavArgId {
    override val key: String = "migrateMode"
    override val navType: NavType<*> = NavType.StringType
}

enum class MigrateModeValue {
    SelectedItems,
    AllVaultItems,
    MoveFolder
}

enum class MigrateVaultFilter {
    All,
    Shared
}

object MigrateVaultFilterArg : OptionalNavArgId {
    override val key = "migrateVaultFilter"
    override val navType = NavType.StringType
}

object MigrateNewParentFolderNavArgId : OptionalNavArgId {
    override val key = "newParentFolderId"
    override val navType = NavType.StringType
}

object MigrateSelectVault : NavItem(
    baseRoute = "migrate/select",
    navArgIds = listOf(MigrateModeArg),
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, MigrateVaultFilterArg, CommonOptionalNavArgId.FolderId),
    navItemType = NavItemType.Bottomsheet,
    noHistory = true
) {

    fun createNavRouteForMigrateAll(shareId: ShareId) = buildString {
        append("$baseRoute/${MigrateModeValue.AllVaultItems.name}")
        val map = mapOf(CommonOptionalNavArgId.ShareId.key to shareId.id)
        append(map.toPath())
    }

    fun createNavRouteForMigrateSelectedItems(filter: MigrateVaultFilter, folderId: Option<FolderId> = None): String =
        buildString {
            append("$baseRoute/${MigrateModeValue.SelectedItems.name}")

            val map = mutableMapOf<String, Any>(
                MigrateVaultFilterArg.key to filter.name
            )
            if (folderId is Some) {
                map[CommonOptionalNavArgId.FolderId.key] = folderId.value.id
            }
            append(map.toPath())
        }

    fun createNavRouteForMoveFolder(shareId: ShareId, folderId: FolderId) = buildString {
        append("$baseRoute/${MigrateModeValue.MoveFolder.name}")
        val map = mutableMapOf<String, Any>(
            CommonOptionalNavArgId.ShareId.key to shareId.id,
            CommonOptionalNavArgId.FolderId.key to folderId.id
        )
        append(map.toPath())
    }
}

object MigrateConfirmVault : NavItem(
    baseRoute = "migrate/confirm",
    navArgIds = listOf(MigrateModeArg, DestinationShareNavArgId),
    optionalArgIds = listOf(
        CommonOptionalNavArgId.ShareId,
        CommonOptionalNavArgId.FolderId,
        MigrateNewParentFolderNavArgId
    ),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRouteForMigrateAll(sourceShareId: ShareId, destShareId: ShareId) = buildString {
        append("$baseRoute/${MigrateModeValue.AllVaultItems.name}/${destShareId.id}")
        val map = mapOf(
            CommonOptionalNavArgId.ShareId.key to sourceShareId.id
        )
        append(map.toPath())
    }

    fun createNavRouteForMigrateSelectedItems(destShareId: ShareId): String =
        "$baseRoute/${MigrateModeValue.SelectedItems.name}/${destShareId.id}"

    fun createNavRouteForMoveFolder(
        shareId: ShareId,
        folderId: FolderId,
        newParentFolderId: FolderId? = null
    ) = buildString {
        append("$baseRoute/${MigrateModeValue.MoveFolder.name}/${shareId.id}")
        val map = mutableMapOf(
            CommonOptionalNavArgId.FolderId.key to folderId.id
        )
        if (newParentFolderId != null) {
            map[MigrateNewParentFolderNavArgId.key] = newParentFolderId.id
        }
        append(map.toPath())
    }
}

fun NavGraphBuilder.migrateGraph(navigation: (MigrateNavigation) -> Unit) {
    bottomSheet(MigrateSelectVault) {
        MigrateSelectVaultBottomSheet(
            onNavigate = navigation
        )
    }

    bottomSheet(MigrateConfirmVault) {
        MigrateConfirmVaultBottomSheet(
            navigation = navigation
        )
    }

    dialog(MigrateSharedWarningNavItem) {
        MigrateSharedWarningDialog(onNavigate = navigation)
    }
}
