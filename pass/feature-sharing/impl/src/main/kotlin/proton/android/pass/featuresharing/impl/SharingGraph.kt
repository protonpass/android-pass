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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingPermissionsScreen
import proton.android.pass.featuresharing.impl.sharingwith.SharingWithScreen
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ShareId

object EmailNavArgId : NavArgId {
    override val key: String = "email"
    override val navType: NavType<*> = NavType.StringType
}

object SharingWith : NavItem(
    baseRoute = "sharing/with/screen",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
}

object SharingPermissions : NavItem(
    baseRoute = "sharing/permissions/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, EmailNavArgId)
) {
    fun createRoute(shareId: ShareId, email: String) = "$baseRoute/${shareId.id}/$email"
}

sealed interface SharingNavigation {
    object Back : SharingNavigation
    data class Permissions(val shareId: ShareId, val email: String) : SharingNavigation
    data class Summary(val shareId: ShareId, val email: String, val permission: Int) : SharingNavigation
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
}
