package proton.android.pass.featureitemdetail.impl.migrate

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureitemdetail.impl.migrate.confirmvault.MigrateConfirmVaultBottomSheet
import proton.android.pass.featureitemdetail.impl.migrate.selectvault.MigrateSelectVaultBottomSheet
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object MigrateSelectVault : NavItem(
    baseRoute = "migrate/select",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}

object MigrateConfirmVault : NavItem(
    baseRoute = "migrate/confirm",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId, DestinationShareNavArgId)
) {
    fun createNavRoute(sourceShareId: ShareId, itemId: ItemId, destShareId: ShareId) =
        "$baseRoute/${sourceShareId.id}/${itemId.id}/${destShareId.id}"
}

fun NavGraphBuilder.migrateItemGraph(
    onMigrateVaultSelectedClick: (ShareId, ItemId, ShareId) -> Unit,
    onItemMigrated: (ShareId, ItemId) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit,
) {
    bottomSheet(MigrateSelectVault) {
        MigrateSelectVaultBottomSheet(
            onClose = { dismissBottomSheet({}) },
            onVaultSelected = { sourceShareId: ShareId, itemId: ItemId, destinationShareId: ShareId ->
                dismissBottomSheet {
                    onMigrateVaultSelectedClick(sourceShareId, itemId, destinationShareId)
                }
            },
        )
    }

    bottomSheet(MigrateConfirmVault) {
        MigrateConfirmVaultBottomSheet(
            onCancel = { dismissBottomSheet({}) },
            onMigrated = { shareId: ShareId, itemId: ItemId ->
                dismissBottomSheet {
                    onItemMigrated(shareId, itemId)
                }
            }
        )
    }
}
