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

package proton.android.pass.features.migrate

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.migrate.confirmvault.MigrateConfirmVaultBottomSheet
import proton.android.pass.features.migrate.selectvault.MigrateSelectVaultBottomSheet
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.toPath

sealed interface MigrateNavigation {
    data class VaultSelectedForMigrateItem(
        val destShareId: ShareId
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
    data object Close : MigrateNavigation
}

object MigrateModeArg : NavArgId {
    override val key: String = "migrateMode"
    override val navType: NavType<*> = NavType.StringType
}

enum class MigrateModeValue {
    SelectedItems,
    AllVaultItems
}

enum class MigrateVaultFilter {
    All,
    Shared
}

object MigrateVaultFilterArg : OptionalNavArgId {
    override val key = "migrateVaultFilter"
    override val navType = NavType.StringType
}

object MigrateSelectVault : NavItem(
    baseRoute = "migrate/select",
    navArgIds = listOf(MigrateModeArg),
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, MigrateVaultFilterArg),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRouteForMigrateAll(shareId: ShareId) = buildString {
        append("$baseRoute/${MigrateModeValue.AllVaultItems.name}")

        val map = mapOf(
            CommonOptionalNavArgId.ShareId.key to shareId.id
        )
        append(map.toPath())
    }

    fun createNavRouteForMigrateSelectedItems(filter: MigrateVaultFilter): String = buildString {
        append("$baseRoute/${MigrateModeValue.SelectedItems.name}")

        val map = mapOf(
            MigrateVaultFilterArg.key to filter.name
        )
        append(map.toPath())
    }

}

object MigrateConfirmVault : NavItem(
    baseRoute = "migrate/confirm",
    navArgIds = listOf(MigrateModeArg, DestinationShareNavArgId),
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId),
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
}

