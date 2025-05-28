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

package proton.android.pass.features.itemdetail

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemdetail.common.CannotPerformActionDialogType
import proton.android.pass.features.itemdetail.login.reusedpass.navigation.LoginItemDetailsReusedPassNavItem
import proton.android.pass.features.itemdetail.login.reusedpass.ui.LoginItemDetailReusedPassScreen
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.composable

sealed interface ItemDetailNavigation {

    data class OnEdit(val itemUiModel: ItemUiModel) : ItemDetailNavigation

    data object OnMigrate : ItemDetailNavigation

    data object OnMigrateSharedWarning : ItemDetailNavigation

    data class OnCreateLoginFromAlias(
        val alias: String,
        val shareId: ShareId
    ) : ItemDetailNavigation

    data class OnViewItem(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data object CloseScreen : ItemDetailNavigation
    data object DismissBottomSheet : ItemDetailNavigation

    @JvmInline
    value class Upgrade(val popBefore: Boolean = false) : ItemDetailNavigation

    data class ManageItem(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    @JvmInline
    value class ManageVault(val shareId: ShareId) : ItemDetailNavigation

    data class OnShareVault(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data class OnViewItemHistory(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailNavigation

    data class ViewPasskeyDetails(
        val shareId: ShareId,
        val itemId: ItemId,
        val passkeyId: PasskeyId
    ) : ItemDetailNavigation

    data class ViewReusedPasswords(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data class OnTrashAlias(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data class OnContactsClicked(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    @JvmInline
    value class LeaveItemShare(val shareId: ShareId) : ItemDetailNavigation

    data class OpenAttachmentOptions(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : ItemDetailNavigation
}

enum class ItemDetailCannotPerformActionType {
    CannotEditBecauseNoPermissions,
    CannotEditBecauseNeedsUpgrade,
    CannotEditBecauseItemInTrash,
    CannotShareBecauseLimitReached,
    CannotShareBecauseNoPermissions,
    CannotShareBecauseItemInTrash;

    fun toType(): CannotPerformActionDialogType = when (this) {
        CannotEditBecauseNoPermissions -> CannotPerformActionDialogType.CannotEditBecauseNoPermissions
        CannotEditBecauseNeedsUpgrade -> CannotPerformActionDialogType.CannotEditBecauseNeedsUpgrade
        CannotEditBecauseItemInTrash -> CannotPerformActionDialogType.CannotEditBecauseItemInTrash
        CannotShareBecauseLimitReached -> CannotPerformActionDialogType.CannotShareBecauseLimitReached
        CannotShareBecauseNoPermissions -> CannotPerformActionDialogType.CannotShareBecauseNoPermissions
        CannotShareBecauseItemInTrash -> CannotPerformActionDialogType.CannotShareBecauseItemInTrash
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

object ItemDetailScopeNavArgId : OptionalNavArgId {
    override val key: String = "itemDetailNavScope"
    override val navType: NavType<*> = NavType.EnumType(ItemDetailNavScope::class.java)
    override val default: Any = ItemDetailNavScope.Default
}

fun NavGraphBuilder.itemDetailGraph(onNavigate: (ItemDetailNavigation) -> Unit) {

    composable(navItem = LoginItemDetailsReusedPassNavItem) {
        LoginItemDetailReusedPassScreen(onNavigated = onNavigate)
    }

}
