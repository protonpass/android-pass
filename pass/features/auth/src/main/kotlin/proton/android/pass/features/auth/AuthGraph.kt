/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navigation
import me.proton.core.domain.entity.UserId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

const val AUTH_GRAPH = "auth_graph"
private const val AUTH_ORIGIN_ARG = "authOrigin"
private const val AUTH_BASE_ROUTE = "auth"

object AuthOriginNavArgId : NavArgId {
    override val key: String = AUTH_ORIGIN_ARG
    override val navType: NavType<*> = NavType.EnumType(AuthOrigin::class.java)
}

data class AuthOriginWithDefaultNavArgId(override val default: AuthOrigin) : NavArgId {
    override val key: String = AUTH_ORIGIN_ARG
    override val navType: NavType<*> = NavType.EnumType(AuthOrigin::class.java)
}

data class UserIdWithDefaultNavArgId(override val default: String) : NavArgId {
    override val key: String = CommonNavArgId.UserId.key
    override val navType: NavType<*> = NavType.StringType
}

object Auth : NavItem(
    baseRoute = AUTH_BASE_ROUTE,
    navArgIds = listOf(AuthOriginNavArgId),
    noHistory = true
) {
    fun buildRoute(origin: AuthOrigin): String = "$baseRoute/${origin.name}"
}

data class AuthWithDefault(val origin: AuthOrigin, val userId: UserId) : NavItem(
    baseRoute = AUTH_BASE_ROUTE,
    navArgIds = listOf(AuthOriginWithDefaultNavArgId(origin), UserIdWithDefaultNavArgId(userId.id)),
    noHistory = true
)

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

    data object CloseBottomsheet : AuthNavigation

    @JvmInline
    value class SignOut(val userId: UserId) : AuthNavigation

    @JvmInline
    value class ForceSignOut(val userId: UserId) : AuthNavigation

    data object ForceSignOutAllUsers : AuthNavigation

    @JvmInline
    value class EnterPin(val origin: AuthOrigin) : AuthNavigation

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
                        is EnterPinNavigation.CloseBottomsheet ->
                            navigation(AuthNavigation.CloseBottomsheet)
                        EnterPinNavigation.ForceSignOutAllUsers ->
                            navigation(AuthNavigation.ForceSignOutAllUsers)
                    }
                }
            )
        }

    }
}
