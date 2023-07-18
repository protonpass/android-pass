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

package proton.android.pass.featureauth.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

object Auth : NavItem(baseRoute = "auth", noHistory = true)
object EnterPin : NavItem(
    baseRoute = "pin/enter/bottomsheet",
    noHistory = true,
    navItemType = NavItemType.Bottomsheet
)

sealed interface AuthNavigation {
    object Success : AuthNavigation
    object Failed : AuthNavigation
    object Dismissed : AuthNavigation
    object SignOut : AuthNavigation
    object ForceSignOut : AuthNavigation
    object EnterPin : AuthNavigation
    object Back : AuthNavigation
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.authGraph(
    canLogout: Boolean,
    navigation: (AuthNavigation) -> Unit
) {
    composable(Auth) {
        BackHandler { navigation(AuthNavigation.Back) }
        AuthScreen(
            canLogout = canLogout,
            navigation = navigation
        )
    }
    bottomSheet(EnterPin) {
        EnterPinBottomsheet(
            onNavigate = {
                when (it) {
                    EnterPinNavigation.Success -> navigation(AuthNavigation.Success)
                    EnterPinNavigation.ForceSignOut -> navigation(AuthNavigation.ForceSignOut)
                }
            }
        )
    }
}
