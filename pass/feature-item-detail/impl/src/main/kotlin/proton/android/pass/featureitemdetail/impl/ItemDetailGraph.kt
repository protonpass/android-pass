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

package proton.android.pass.featureitemdetail.impl

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import me.proton.core.compose.navigation.requireArguments
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemdetail.impl.common.CannotPerformActionDialog
import proton.android.pass.featureitemdetail.impl.common.CannotPerformActionDialogType
import proton.android.pass.featureitemdetail.impl.login.passkey.bottomsheet.navigation.passkeyDetailBottomSheetGraph
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog

sealed interface ItemDetailNavigation {

    data class OnEdit(val itemUiModel: ItemUiModel) : ItemDetailNavigation

    data object OnMigrate : ItemDetailNavigation

    data class OnCreateLoginFromAlias(
        val alias: String,
        val shareId: ShareId
    ) : ItemDetailNavigation

    data class OnViewItem(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data object Back : ItemDetailNavigation
    data object CloseBottomSheet : ItemDetailNavigation

    @JvmInline
    value class Upgrade(val popBefore: Boolean = false) : ItemDetailNavigation

    @JvmInline
    value class ManageVault(val shareId: ShareId) : ItemDetailNavigation

    data class OnShareVault(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    @JvmInline
    value class CannotPerformAction(
        val type: ItemDetailCannotPerformActionType
    ) : ItemDetailNavigation

    data class OnViewItemHistory(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailNavigation

    data class ViewPasskeyDetails(
        val shareId: ShareId,
        val itemId: ItemId,
        val passkeyId: PasskeyId
    ) : ItemDetailNavigation
}

enum class ItemDetailCannotPerformActionType {
    CannotEditBecauseNoPermissions,
    CannotEditBecauseNeedsUpgrade,
    CannotEditBecauseItemInTrash,
    CannotShareBecauseLimitReached,
    CannotShareBecauseNoPermissions;

    fun toType(): CannotPerformActionDialogType = when (this) {
        CannotEditBecauseNoPermissions -> CannotPerformActionDialogType.CannotEditBecauseNoPermissions
        CannotEditBecauseNeedsUpgrade -> CannotPerformActionDialogType.CannotEditBecauseNeedsUpgrade
        CannotEditBecauseItemInTrash -> CannotPerformActionDialogType.CannotEditBecauseItemInTrash
        CannotShareBecauseLimitReached -> CannotPerformActionDialogType.CannotShareBecauseLimitReached
        CannotShareBecauseNoPermissions -> CannotPerformActionDialogType.CannotShareBecauseNoPermissions
    }
}

enum class ItemDetailNavScope {
    Default,
    MonitorExcluded,
    MonitorReport,
    MonitorWeakPassword,
    MonitorReusedPassword,
    MonitorMissing2fa
}

object ItemDetailCannotPerformActionTypeNavArgId : NavArgId {
    override val key = "cannotPerformActionType"
    override val navType = NavType.StringType
}

object ItemDetailCannotPerformAction : NavItem(
    baseRoute = "item/detail/cannotperformaction/dialog",
    navArgIds = listOf(ItemDetailCannotPerformActionTypeNavArgId),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(type: ItemDetailCannotPerformActionType) = "$baseRoute/${type.name}"
}

object ItemDetailScopeNavArgId : NavArgId {
    override val key: String = "itemDetailNavScope"
    override val navType: NavType<*> = NavType.EnumType(ItemDetailNavScope::class.java)
}

object ViewItem : NavItem(
    baseRoute = "item/detail/view",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId, ItemDetailScopeNavArgId)
) {
    fun createNavRoute(
        shareId: ShareId,
        itemId: ItemId,
        scope: ItemDetailNavScope = ItemDetailNavScope.Default
    ) = "$baseRoute/${shareId.id}/${itemId.id}/$scope"
}

fun NavGraphBuilder.itemDetailGraph(onNavigate: (ItemDetailNavigation) -> Unit) {
    composable(
        navItem = ViewItem
    ) {
        ItemDetailScreen(
            onNavigate = onNavigate
        )
    }

    dialog(ItemDetailCannotPerformAction) { backStackEntry ->
        val type = remember {
            val typeArg: String = requireNotNull(
                backStackEntry
                    .requireArguments()
                    .getString(ItemDetailCannotPerformActionTypeNavArgId.key)
            )
            val asType = ItemDetailCannotPerformActionType.entries.find { it.name == typeArg }
                ?: throw IllegalStateException("Cannot find type $typeArg")
            asType.toType()
        }
        CannotPerformActionDialog(
            type = type,
            onClose = { onNavigate(ItemDetailNavigation.Back) },
            onUpgrade = { onNavigate(ItemDetailNavigation.Upgrade(true)) }
        )
    }

    passkeyDetailBottomSheetGraph(
        onDismiss = { onNavigate(ItemDetailNavigation.CloseBottomSheet) }
    )
}
