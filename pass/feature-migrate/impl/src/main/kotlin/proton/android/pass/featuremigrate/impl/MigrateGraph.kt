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
    optionalArgIds = listOf(CommonOptionalNavArgId.ItemId)
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
    optionalArgIds = listOf(CommonOptionalNavArgId.ItemId)
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
    dismissBottomSheet: (() -> Unit) -> Unit,
) {
    bottomSheet(MigrateSelectVault) {
        MigrateSelectVaultBottomSheet(
            navigation = navigation,
            onClose = { dismissBottomSheet({}) },
        )
    }

    bottomSheet(MigrateConfirmVault) {
        MigrateConfirmVaultBottomSheet(
            navigation = navigation,
            onCancel = { dismissBottomSheet({}) },
        )
    }
}

