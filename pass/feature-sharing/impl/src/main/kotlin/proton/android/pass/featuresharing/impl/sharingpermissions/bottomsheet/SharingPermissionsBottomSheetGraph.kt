/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featuresharing.impl.sharingpermissions.bottomsheet

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featuresharing.impl.SharingNavigation
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingType
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.toPath

enum class EditPermissionsMode {
    SingleUser,
    AllUsers
}

object EditPermissionsModeNavArgId : NavArgId {
    override val key = "editPermissionsMode"
    override val navType = NavType.StringType
}

object EmailNavArgId : OptionalNavArgId {
    override val key: String = "email"
    override val navType: NavType<*> = NavType.StringType
}

object PermissionNavArgId : OptionalNavArgId {
    override val key: String = "permission"
    override val navType: NavType<*> = NavType.StringType
}

object SharingEditPermissions : NavItem(
    baseRoute = "sharing/permissions/bottomsheet",
    navArgIds = listOf(EditPermissionsModeNavArgId),
    optionalArgIds = listOf(EmailNavArgId, PermissionNavArgId),
    navItemType = NavItemType.Bottomsheet,
) {
    fun buildRouteForEditAll(): String = "$baseRoute/${EditPermissionsMode.AllUsers.name}"
    fun buildRouteForEditOne(email: String, permission: SharingType) = buildString {
        append("$baseRoute/${EditPermissionsMode.SingleUser.name}")

        val params = mapOf(EmailNavArgId.key to email, PermissionNavArgId.key to permission.name)
        append(params.toPath())
    }
}

fun NavGraphBuilder.sharingPermissionsBottomsheetGraph(
    onNavigateEvent: (SharingNavigation) -> Unit
) {
    bottomSheet(SharingEditPermissions) {
        SharingPermissionsBottomSheet(onNavigate = onNavigateEvent)
    }
}
