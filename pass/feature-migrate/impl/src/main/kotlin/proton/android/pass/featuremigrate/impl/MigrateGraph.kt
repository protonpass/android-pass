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

package proton.android.pass.featuremigrate.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featuremigrate.impl.confirmvault.MigrateConfirmVaultBottomSheet
import proton.android.pass.featuremigrate.impl.selectvault.MigrateSelectVaultBottomSheet
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

sealed interface MigrateNavigation {
    data class VaultSelectedForMigrateItem(
        val sourceShareId: ShareId,
        val destShareId: ShareId,
        val itemId: ItemId
    ) : MigrateNavigation
    data class VaultSelectedForMigrateAll(
        val sourceShareId: ShareId,
        val destShareId: ShareId
    ) : MigrateNavigation
    data class ItemMigrated(
        val shareId: ShareId,
        val itemId: ItemId
    ) : MigrateNavigation
    object VaultMigrated : MigrateNavigation
    object Close : MigrateNavigation
}

object MigrateModeArg : NavArgId {
    override val key: String = "migrateMode"
    override val navType: NavType<*> = NavType.StringType
}

enum class MigrateModeValue {
    SingleItem,
    AllVaultItems;
}

object MigrateSelectVault : NavItem(
    baseRoute = "migrate/select",
    navArgIds = listOf(CommonNavArgId.ShareId, MigrateModeArg),
    optionalArgIds = listOf(CommonOptionalNavArgId.ItemId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRouteForMigrateAll(shareId: ShareId) =
        "$baseRoute/${shareId.id}/${MigrateModeValue.AllVaultItems.name}"

    fun createNavRouteForMigrateItem(shareId: ShareId, itemId: ItemId): String = buildString {
        append("$baseRoute/${shareId.id}/${MigrateModeValue.SingleItem.name}")

        val map = mapOf(CommonOptionalNavArgId.ItemId.key to itemId.id)
        append(map.toPath())
    }

}

object MigrateConfirmVault : NavItem(
    baseRoute = "migrate/confirm",
    navArgIds = listOf(CommonNavArgId.ShareId, MigrateModeArg, DestinationShareNavArgId),
    optionalArgIds = listOf(CommonOptionalNavArgId.ItemId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRouteForMigrateAll(shareId: ShareId, destShareId: ShareId) = buildString {
        append("$baseRoute/${shareId.id}/${MigrateModeValue.AllVaultItems.name}/${destShareId.id}")
    }

    fun createNavRouteForMigrateItem(shareId: ShareId, itemId: ItemId, destShareId: ShareId): String = buildString {
        append("$baseRoute/${shareId.id}/${MigrateModeValue.SingleItem.name}/${destShareId.id}")

        val map = mapOf(CommonOptionalNavArgId.ItemId.key to itemId.id)
        append(map.toPath())
    }
}

fun NavGraphBuilder.migrateGraph(
    navigation: (MigrateNavigation) -> Unit,
) {
    bottomSheet(MigrateSelectVault) {
        MigrateSelectVaultBottomSheet(
            onNavigate = navigation,
        )
    }

    bottomSheet(MigrateConfirmVault) {
        MigrateConfirmVaultBottomSheet(
            navigation = navigation
        )
    }
}

