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

package proton.android.pass.features.sharing

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.Option
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.shares.SharePendingInvite
import proton.android.pass.features.sharing.accept.AcceptInviteBottomSheet
import proton.android.pass.features.sharing.invitesinfo.InvitesErrorDialog
import proton.android.pass.features.sharing.invitesinfo.InvitesInfoDialog
import proton.android.pass.features.sharing.manage.ManageVaultScreen
import proton.android.pass.features.sharing.manage.bottomsheet.memberOptionsBottomSheetGraph
import proton.android.pass.features.sharing.manage.item.navigation.ManageItemNavItem
import proton.android.pass.features.sharing.manage.item.ui.ManageItemScreen
import proton.android.pass.features.sharing.manage.iteminviteoptions.navigation.ManageItemInviteOptionsNavItem
import proton.android.pass.features.sharing.manage.iteminviteoptions.ui.ManageItemInviteOptionsBottomSheet
import proton.android.pass.features.sharing.manage.itemmemberoptions.navigation.ManageItemMemberOptionsNavItem
import proton.android.pass.features.sharing.manage.itemmemberoptions.ui.ManageItemMemberOptionsBottomSheet
import proton.android.pass.features.sharing.sharefromitem.ShareFromItemBottomSheet
import proton.android.pass.features.sharing.sharingpermissions.SharingPermissionsScreen
import proton.android.pass.features.sharing.sharingpermissions.bottomsheet.sharingPermissionsBottomsheetGraph
import proton.android.pass.features.sharing.sharingsummary.SharingSummaryScreen
import proton.android.pass.features.sharing.sharingwith.SharingWithScreen
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog
import proton.android.pass.navigation.api.toPath

object ShowEditVaultArgId : NavArgId {
    override val key: String = "show_edit_vault"
    override val navType: NavType<*> = NavType.BoolType
}

object SharingWith : NavItem(
    baseRoute = "sharing/with/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, ShowEditVaultArgId),
    optionalArgIds = listOf(CommonOptionalNavArgId.ItemId)
) {
    fun createRoute(
        shareId: ShareId,
        showEditVault: Boolean,
        itemIdOption: Option<ItemId>
    ): String = buildString {
        append("$baseRoute/${shareId.id}/$showEditVault")

        itemIdOption.value()?.let { itemId ->
            mapOf(CommonOptionalNavArgId.ItemId.key to itemId.id)
                .toPath()
                .also(::append)
        }
    }
}

object SharingPermissions : NavItem(
    baseRoute = "sharing/permissions/screen",
    navArgIds = listOf(CommonNavArgId.ShareId),
    optionalArgIds = listOf(CommonOptionalNavArgId.ItemId)
) {
    fun createRoute(shareId: ShareId, itemIdOption: Option<ItemId>) = buildString {
        append("$baseRoute/${shareId.id}")

        itemIdOption.value()?.let { itemId ->
            mapOf(CommonOptionalNavArgId.ItemId.key to itemId.id)
                .toPath()
                .also(::append)
        }
    }
}

object AcceptInvite : NavItem(
    baseRoute = "sharing/accept",
    navArgIds = listOf(CommonNavArgId.InviteToken),
    navItemType = NavItemType.Bottomsheet
) {

    fun createRoute(inviteToken: InviteToken) = "$baseRoute/${inviteToken.value}"

}

object SharingSummary : NavItem(
    baseRoute = "sharing/summary/screen",
    navArgIds = listOf(CommonNavArgId.ShareId),
    optionalArgIds = listOf(CommonOptionalNavArgId.ItemId)
) {
    fun createRoute(shareId: ShareId, itemIdOption: Option<ItemId>) = buildString {
        append("$baseRoute/${shareId.id}")

        itemIdOption.value()?.let { itemId ->
            mapOf(CommonOptionalNavArgId.ItemId.key to itemId.id)
                .toPath()
                .also(::append)
        }
    }
}

object ManageVault : NavItem(
    baseRoute = "sharing/manage/screen",
    navArgIds = listOf(CommonNavArgId.ShareId),
    baseDeepLinkRoute = listOf("share_members")
) {
    fun createRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

object InvitesInfoDialog : NavItem(
    baseRoute = "sharing/manage/invites/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

object InvitesErrorDialog : NavItem(
    baseRoute = "sharing/manage/invites/error/dialog",
    navItemType = NavItemType.Dialog
)

object ShareFromItem : NavItem(
    baseRoute = "sharing/fromitem/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

sealed interface SharingNavigation {

    data object Back : SharingNavigation

    data object BackToHome : SharingNavigation

    data object Upgrade : SharingNavigation

    @JvmInline
    value class ShowInvitesInfo(val shareId: ShareId) : SharingNavigation

    @JvmInline
    value class CloseBottomSheet(val refresh: Boolean) : SharingNavigation

    data class Permissions(val shareId: ShareId, val itemIdOption: Option<ItemId>) :
        SharingNavigation

    data class Summary(val shareId: ShareId, val itemIdOption: Option<ItemId>) : SharingNavigation

    @JvmInline
    value class ShareVault(val shareId: ShareId) : SharingNavigation

    data class ShareItem(val shareId: ShareId, val itemId: ItemId) : SharingNavigation

    data class ShareItemLink(val shareId: ShareId, val itemId: ItemId) : SharingNavigation

    @JvmInline
    value class ManageItem(val shareId: ShareId) : SharingNavigation

    data class ManageItemMemberOptions(
        val shareId: ShareId,
        val memberRole: ShareRole,
        val memberShareId: ShareId,
        val memberEmail: String
    ) : SharingNavigation

    data class ManageItemInviteOptions(
        val shareId: ShareId,
        val pendingInvite: SharePendingInvite
    ) : SharingNavigation

    @JvmInline
    value class ManageVault(val shareId: ShareId) : SharingNavigation

    data class MemberOptions(
        val shareId: ShareId,
        val memberRole: ShareRole,
        val destShareId: ShareId,
        val destEmail: String
    ) : SharingNavigation

    data class ExistingUserInviteOptions(
        val shareId: ShareId,
        val inviteId: InviteId
    ) : SharingNavigation

    data class NewUserInviteOptions(
        val shareId: ShareId,
        val inviteId: NewUserInviteId
    ) : SharingNavigation

    data class TransferOwnershipConfirm(
        val shareId: ShareId,
        val destShareId: ShareId,
        val destEmail: String
    ) : SharingNavigation

    data object MoveItemToSharedVault : SharingNavigation

    data class CreateVaultAndMoveItem(
        val shareId: ShareId,
        val itemId: ItemId
    ) : SharingNavigation

    @JvmInline
    value class EditVault(val shareId: ShareId) : SharingNavigation

    @JvmInline
    value class ViewVault(val shareId: ShareId) : SharingNavigation

    data class InviteToVaultEditPermissions(
        val email: String,
        val permission: ShareRole
    ) : SharingNavigation

    data object InviteToVaultEditAllPermissions : SharingNavigation

    data object InviteError : SharingNavigation

    @JvmInline
    value class Upsell(val paidFeature: PaidFeature) : SharingNavigation

    @JvmInline
    value class ManageSharedVault(val sharedVaultId: ShareId) : SharingNavigation

    @JvmInline
    value class ItemDetails(val itemCategory: ItemCategory) : SharingNavigation

    data class SharedItemDetails(val shareId: ShareId, val itemId: ItemId) : SharingNavigation
}

fun NavGraphBuilder.sharingGraph(onNavigateEvent: (SharingNavigation) -> Unit) {
    composable(SharingWith) {
        SharingWithScreen(onNavigateEvent = onNavigateEvent)
    }

    composable(SharingPermissions) {
        SharingPermissionsScreen(onNavigateEvent = onNavigateEvent)
    }

    composable(SharingSummary) {
        SharingSummaryScreen(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(AcceptInvite) {
        BackHandler { onNavigateEvent(SharingNavigation.BackToHome) }
        AcceptInviteBottomSheet(onNavigateEvent = onNavigateEvent)
    }

    composable(ManageVault) {
        val refresh by it.savedStateHandle
            .getStateFlow(REFRESH_MEMBER_LIST_FLAG, false)
            .collectAsStateWithLifecycle()
        ManageVaultScreen(
            refresh = refresh,
            onNavigateEvent = onNavigateEvent,
            clearRefreshFlag = { it.savedStateHandle.remove<String>(REFRESH_MEMBER_LIST_FLAG) }
        )
    }

    composable(navItem = ManageItemNavItem) {
        ManageItemScreen(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(navItem = ManageItemMemberOptionsNavItem) {
        ManageItemMemberOptionsBottomSheet(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(navItem = ManageItemInviteOptionsNavItem) {
        ManageItemInviteOptionsBottomSheet(onNavigateEvent = onNavigateEvent)
    }

    dialog(InvitesInfoDialog) {
        BackHandler { onNavigateEvent(SharingNavigation.CloseBottomSheet(false)) }
        InvitesInfoDialog(onNavigateEvent = onNavigateEvent)
    }

    dialog(InvitesErrorDialog) {
        InvitesErrorDialog(onNavigateEvent = onNavigateEvent)
    }

    bottomSheet(ShareFromItem) {
        ShareFromItemBottomSheet(onNavigateEvent = onNavigateEvent)
    }

    memberOptionsBottomSheetGraph(onNavigateEvent)
    sharingPermissionsBottomsheetGraph(onNavigateEvent)
}

const val REFRESH_MEMBER_LIST_FLAG = "refreshMemberList"
