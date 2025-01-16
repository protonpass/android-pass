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
import proton.android.pass.features.migrate.warningshared.navigation.MigrateSharedWarningNavItem
import proton.android.pass.features.migrate.warningshared.ui.MigrateSharedWarningDialog
import proton.android.pass.navigation.api.CommonNavArgId
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

    data object DismissBottomsheet : MigrateNavigation

    data class VaultSelection(
        val migrateModeValue: MigrateModeValue,
        val shareId: ShareId?,
        val filter: MigrateVaultFilter?
    ) : MigrateNavigation

}

object MigrateModeArg : NavArgId {

    override val key: String = "migrateMode"

    override val navType: NavType<*> = NavType.EnumType(MigrateModeValue::class.java)

}

enum class MigrateModeValue {
    AllVaultItems,
    SelectedItems
}

enum class MigrateVaultFilter {
    All,
    Shared
}

object MigrateVaultFilterArg : OptionalNavArgId {

    override val key: String = "migrateVaultFilter"

    override val navType: NavType<*> = NavType.EnumType(MigrateVaultFilter::class.java)

}

object MigrateSelectVault : NavItem(
    baseRoute = "migrate/select",
    navArgIds = listOf(MigrateModeArg),
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, MigrateVaultFilterArg),
    navItemType = NavItemType.Bottomsheet,
    noHistory = true
) {

    fun createNavRoute(
        migrateMode: MigrateModeValue,
        shareId: ShareId? = null,
        filter: MigrateVaultFilter? = null
    ): String = buildString {
        append("$baseRoute/$migrateMode")

        buildMap {
            if (shareId != null) {
                put(CommonNavArgId.ShareId.key, shareId.id)
            }

            if (filter != null) {
                put(MigrateVaultFilterArg.key, filter)
            }
        }
            .toPath()
            .also(::append)
    }

}

object MigrateConfirmVault : NavItem(
    baseRoute = "migrate/confirm",
    navArgIds = listOf(
        MigrateModeArg,
        DestinationShareNavArgId
    ),
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId),
    navItemType = NavItemType.Bottomsheet
) {

    fun createNavRoute(
        migrateMode: MigrateModeValue,
        destShareId: ShareId,
        sourceShareId: ShareId? = null
    ): String = buildString {
        append("$baseRoute/$migrateMode/${destShareId.id}")

        sourceShareId?.let { shareId ->
            mapOf(CommonOptionalNavArgId.ShareId.key to shareId.id)
                .toPath()
                .also(::append)
        }
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
