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

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navigation
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

const val AUTH_GRAPH = "auth_graph"

object AuthOriginNavArgId : NavArgId {
    override val key: String = "authOrigin"
    override val navType: NavType<*> = NavType.EnumType(AuthOrigin::class.java)
}

object Auth : NavItem(
    baseRoute = "auth",
    navArgIds = listOf(AuthOriginNavArgId),
    noHistory = true
) {
    fun buildRoute(origin: AuthOrigin): String = "$baseRoute/${origin.name}"
}

object EnterPin : NavItem(
    baseRoute = "pin/enter/bottomsheet",
    noHistory = true,
    navArgIds = listOf(AuthOriginNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun buildRoute(origin: AuthOrigin): String = "$baseRoute/${origin.name}"
}

sealed interface AuthNavigation {

    @JvmInline
    value class Success(val origin: AuthOrigin) : AuthNavigation

    data object Failed : AuthNavigation

    data object Dismissed : AuthNavigation

    data object SignOut : AuthNavigation

    data object ForceSignOut : AuthNavigation

    data object EnterPin : AuthNavigation

    @JvmInline
    value class Back(val origin: AuthOrigin) : AuthNavigation

}

fun NavGraphBuilder.authGraph(canLogout: Boolean, navigation: (AuthNavigation) -> Unit) {
    navigation(
        route = AUTH_GRAPH,
        startDestination = Auth.buildRoute(AuthOrigin.AUTO_LOCK)
    ) {
        composable(Auth) {
            AuthScreen(
                canLogout = canLogout,
                navigation = navigation
            )
        }

        bottomSheet(EnterPin) {
            EnterPinBottomsheet(
                onNavigate = { destination ->
                    when (destination) {
                        is EnterPinNavigation.Success ->
                            navigation(AuthNavigation.Success(destination.origin))
                        EnterPinNavigation.ForceSignOut -> navigation(AuthNavigation.ForceSignOut)
                    }
                }
            )
        }

    }
}
