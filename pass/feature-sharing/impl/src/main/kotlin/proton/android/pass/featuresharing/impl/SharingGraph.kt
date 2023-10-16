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

package proton.android.pass.featuresharing.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featuresharing.impl.accept.AcceptInviteBottomSheet
import proton.android.pass.featuresharing.impl.manage.ManageVaultScreen
import proton.android.pass.featuresharing.impl.manage.bottomsheet.memberOptionsBottomSheetGraph
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingPermissionsScreen
import proton.android.pass.featuresharing.impl.sharingsummary.SharingSummaryScreen
import proton.android.pass.featuresharing.impl.sharingwith.SharingWithScreen
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.pass.domain.InviteId
import proton.pass.domain.NewUserInviteId
import proton.pass.domain.ShareId
import proton.pass.domain.ShareRole

object EmailNavArgId : NavArgId {
    override val key: String = "email"
    override val navType: NavType<*> = NavType.StringType
}

object PermissionNavArgId : NavArgId {
    override val key: String = "permission"
    override val navType: NavType<*> = NavType.IntType
}

object SharingWithUserModeArgId : NavArgId {
    override val key: String = "sharing_with_user_mode"
    override val navType: NavType<*> = NavType.StringType
}

object SharingWith : NavItem(
    baseRoute = "sharing/with/screen",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

enum class SharingWithUserModeType {
    ExistingUser,
    NewUser
}

object SharingPermissions : NavItem(
    baseRoute = "sharing/permissions/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, EmailNavArgId, SharingWithUserModeArgId)
) {
    fun createRoute(shareId: ShareId, email: String, mode: SharingWithUserModeType) =
        "$baseRoute/${shareId.id}/$email/${mode.name}"
}

object AcceptInvite : NavItem("sharing/accept", navItemType = NavItemType.Bottomsheet)

object SharingSummary : NavItem(
    baseRoute = "sharing/summary/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, EmailNavArgId, PermissionNavArgId, SharingWithUserModeArgId)
) {
    fun createRoute(shareId: ShareId, email: String, permission: Int, mode: SharingWithUserModeType) =
        "$baseRoute/${shareId.id}/$email/$permission/${mode.name}"
}

object ManageVault : NavItem(
    baseRoute = "sharing/manage/screen",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

sealed interface SharingNavigation {
    object Back : SharingNavigation

    @JvmInline
    value class CloseBottomSheet(val refresh: Boolean) : SharingNavigation
    data class Permissions(
        val shareId: ShareId,
        val email: String,
        val mode: SharingWithUserModeType
    ) : SharingNavigation

    data class Summary(
        val shareId: ShareId,
        val email: String,
        val permission: Int,
        val mode: SharingWithUserModeType
    ) : SharingNavigation

    @JvmInline
    value class ShareVault(val shareId: ShareId) : SharingNavigation

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
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.sharingGraph(
    onNavigateEvent: (SharingNavigation) -> Unit
) {
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
        BackHandler { onNavigateEvent(SharingNavigation.Back) }
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

    memberOptionsBottomSheetGraph(onNavigateEvent)
}

const val REFRESH_MEMBER_LIST_FLAG = "refreshMemberList"
