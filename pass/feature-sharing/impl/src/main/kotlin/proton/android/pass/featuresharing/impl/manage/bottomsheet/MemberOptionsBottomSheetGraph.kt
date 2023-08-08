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

package proton.android.pass.featuresharing.impl.manage.bottomsheet

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featuresharing.impl.SharingNavigation
import proton.android.pass.featuresharing.impl.manage.bottomsheet.inviteoptions.InviteOptionsBottomSheet
import proton.android.pass.featuresharing.impl.manage.bottomsheet.memberoptions.MemberOptionsBottomSheet
import proton.android.pass.featuresharing.impl.transferownership.TransferOwnershipDialog
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.dialog
import proton.pass.domain.InviteId
import proton.pass.domain.ShareId
import proton.pass.domain.ShareRole

object InviteIdArg : NavArgId {
    override val key = "inviteId"
    override val navType = NavType.StringType
}

object InviteOptionsBottomSheet : NavItem(
    baseRoute = "sharing/manage/invite/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId, InviteIdArg),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(shareId: ShareId, inviteId: InviteId) = "$baseRoute/${shareId.id}/${inviteId.value}"
}


object MemberShareIdArg : NavArgId {
    override val key = "memberShareId"
    override val navType = NavType.StringType
}

object ShareRoleArg : NavArgId {
    override val key = "shareRole"
    override val navType = NavType.StringType
}

object MemberEmailArg : NavArgId {
    override val key = "memberEmail"
    override val navType = NavType.StringType
}

object MemberOptionsBottomSheet : NavItem(
    baseRoute = "sharing/manage/member/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId, MemberShareIdArg, ShareRoleArg, MemberEmailArg),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(shareId: ShareId, memberShareId: ShareId, shareRole: ShareRole, memberEmail: String) =
        "$baseRoute/${shareId.id}/${memberShareId.id}/${shareRole.value}/${NavParamEncoder.encode(memberEmail)}"
}


object ConfirmTransferOwnership : NavItem(
    baseRoute = "sharing/manage/ownership/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId, MemberShareIdArg, MemberEmailArg),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(shareId: ShareId, memberShareId: ShareId, memberEmail: String) =
        "$baseRoute/${shareId.id}/${memberShareId.id}/${NavParamEncoder.encode(memberEmail)}"
}

fun NavGraphBuilder.memberOptionsBottomSheetGraph(
    onNavigateEvent: (SharingNavigation) -> Unit
) {
    bottomSheet(InviteOptionsBottomSheet) {
        BackHandler { onNavigateEvent(SharingNavigation.CloseBottomSheet(refresh = false)) }
        InviteOptionsBottomSheet(onNavigate = onNavigateEvent)
    }

    bottomSheet(MemberOptionsBottomSheet) {
        BackHandler { onNavigateEvent(SharingNavigation.CloseBottomSheet(refresh = false)) }
        MemberOptionsBottomSheet(onNavigate = onNavigateEvent)
    }

    dialog(ConfirmTransferOwnership) {
        BackHandler { onNavigateEvent(SharingNavigation.CloseBottomSheet(refresh = false)) }
        TransferOwnershipDialog(onNavigate = onNavigateEvent)
    }
}
