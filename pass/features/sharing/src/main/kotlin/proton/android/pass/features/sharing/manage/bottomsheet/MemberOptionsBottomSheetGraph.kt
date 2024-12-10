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

package proton.android.pass.features.sharing.manage.bottomsheet

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.sharing.SharingNavigation
import proton.android.pass.features.sharing.manage.bottomsheet.inviteoptions.InviteOptionsBottomSheet
import proton.android.pass.features.sharing.manage.bottomsheet.memberoptions.MemberOptionsBottomSheet
import proton.android.pass.features.sharing.transferownership.TransferOwnershipDialog
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.dialog

object InviteIdArg : NavArgId {
    override val key = "inviteId"
    override val navType = NavType.StringType
}

object InviteTypeArg : NavArgId {
    override val key = "inviteType"
    override val navType = NavType.StringType
}

object IsNewUserInviteArg : NavArgId {

    override val key = "isNewUserInviteKey"

    override val navType = NavType.BoolType

}

sealed interface InviteTypeValue {

    fun type(): String
    fun value(): String

    @JvmInline
    value class ExistingUserInvite(val inviteId: InviteId) : InviteTypeValue {
        override fun type() = INVITE_TYPE_EXISTING_USER
        override fun value() = inviteId.value
    }

    @JvmInline
    value class NewUserInvite(val inviteId: NewUserInviteId) : InviteTypeValue {
        override fun type() = INVITE_TYPE_NEW_USER
        override fun value() = inviteId.value
    }

    companion object {
        const val INVITE_TYPE_EXISTING_USER = "existingUser"
        const val INVITE_TYPE_NEW_USER = "newUser"
    }
}

object InviteOptionsBottomSheet : NavItem(
    baseRoute = "sharing/manage/invite/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId, InviteIdArg, InviteTypeArg),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(shareId: ShareId, inviteType: InviteTypeValue) =
        "$baseRoute/${shareId.id}/${inviteType.value()}/${inviteType.type()}"
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
    fun buildRoute(
        shareId: ShareId,
        memberShareId: ShareId,
        shareRole: ShareRole,
        memberEmail: String
    ) = "$baseRoute/${shareId.id}/${memberShareId.id}/${shareRole.value}/${NavParamEncoder.encode(memberEmail)}"
}


object ConfirmTransferOwnership : NavItem(
    baseRoute = "sharing/manage/ownership/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId, MemberShareIdArg, MemberEmailArg),
    navItemType = NavItemType.Dialog
) {
    fun buildRoute(
        shareId: ShareId,
        memberShareId: ShareId,
        memberEmail: String
    ) = "$baseRoute/${shareId.id}/${memberShareId.id}/${NavParamEncoder.encode(memberEmail)}"
}

fun NavGraphBuilder.memberOptionsBottomSheetGraph(onNavigateEvent: (SharingNavigation) -> Unit) {
    bottomSheet(InviteOptionsBottomSheet) {
        InviteOptionsBottomSheet(onNavigate = onNavigateEvent)
    }

    bottomSheet(MemberOptionsBottomSheet) {
        MemberOptionsBottomSheet(onNavigate = onNavigateEvent)
    }

    dialog(ConfirmTransferOwnership) {
        BackHandler { onNavigateEvent(SharingNavigation.CloseBottomSheet(refresh = false)) }
        TransferOwnershipDialog(onNavigate = onNavigateEvent)
    }
}
