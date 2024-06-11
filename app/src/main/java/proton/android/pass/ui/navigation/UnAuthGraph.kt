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

package proton.android.pass.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import me.proton.core.domain.entity.UserId
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.AuthOrigin
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.featureauth.impl.AuthWithDefault
import proton.android.pass.featureauth.impl.EnterPin
import proton.android.pass.featureauth.impl.EnterPinBottomsheet
import proton.android.pass.featureauth.impl.EnterPinNavigation
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.AppNavigation

internal const val UN_AUTH_GRAPH = "un_auth_graph"

fun NavGraphBuilder.unAuthGraph(
    appNavigator: AppNavigator,
    origin: AuthOrigin,
    userId: UserId? = null,
    startDestination: String = Auth.route,
    onNavigate: (AppNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    navigation(
        route = UN_AUTH_GRAPH,
        startDestination = startDestination
    ) {
        if (userId != null) {
            composable(AuthWithDefault(origin, userId)) {
                SharedAuthScreen(onNavigate, appNavigator)
            }
        } else {
            composable(Auth) {
                SharedAuthScreen(onNavigate, appNavigator)
            }
        }
        bottomSheet(EnterPin) {
            EnterPinBottomsheet(
                onNavigate = {
                    when (it) {
                        is EnterPinNavigation.Success -> dismissBottomSheet { appNavigator.navigateBack() }
                        EnterPinNavigation.ForceSignOut -> onNavigate(AppNavigation.ForceSignOut)
                    }
                }
            )
        }
    }
}

@Composable
private fun SharedAuthScreen(onNavigate: (AppNavigation) -> Unit, appNavigator: AppNavigator) {
    AuthScreen(
        canLogout = true,
        navigation = {
            when (it) {
                AuthNavigation.Dismissed,
                is AuthNavigation.Back -> onNavigate(AppNavigation.Finish)

                is AuthNavigation.Success -> if (it.origin == AuthOrigin.EXTRA_PASSWORD_LOGIN) {
                    onNavigate(AppNavigation.Finish)
                }
                AuthNavigation.Failed -> appNavigator.navigateBack()
                AuthNavigation.SignOut -> onNavigate(AppNavigation.SignOut())
                AuthNavigation.ForceSignOut -> onNavigate(AppNavigation.ForceSignOut)
                is AuthNavigation.EnterPin -> appNavigator.navigate(
                    EnterPin,
                    EnterPin.buildRoute(AuthOrigin.AUTO_LOCK)
                )
            }
        }
    )
}
