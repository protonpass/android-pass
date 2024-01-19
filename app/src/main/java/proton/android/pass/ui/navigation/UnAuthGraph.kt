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

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.featureaccount.impl.AccountNavigation
import proton.android.pass.featureaccount.impl.ConfirmSignOutDialog
import proton.android.pass.featureaccount.impl.SignOutDialog
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.featureauth.impl.EnterPin
import proton.android.pass.featureauth.impl.EnterPinBottomsheet
import proton.android.pass.featureauth.impl.EnterPinNavigation
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog
import proton.android.pass.ui.AppNavigation

internal const val UN_AUTH_GRAPH = "un_auth_graph"

@Suppress("ComplexMethod")
fun NavGraphBuilder.unAuthGraph(
    appNavigator: AppNavigator,
    onNavigate: (AppNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit,
) {
    navigation(
        route = UN_AUTH_GRAPH,
        startDestination = Auth.route
    ) {
        composable(Auth) {
            AuthScreen(
                canLogout = true,
                navigation = {
                    when (it) {
                        AuthNavigation.Dismissed,
                        AuthNavigation.Back -> onNavigate(AppNavigation.Finish)

                        AuthNavigation.Success,
                        AuthNavigation.Failed -> {
                        }

                        AuthNavigation.SignOut -> appNavigator.navigate(SignOutDialog)
                        AuthNavigation.ForceSignOut -> onNavigate(AppNavigation.SignOut())
                        AuthNavigation.EnterPin -> appNavigator.navigate(EnterPin)
                    }
                }
            )
        }

        dialog(SignOutDialog) {
            ConfirmSignOutDialog(
                onNavigate = {
                    when (it) {
                        AccountNavigation.Back -> appNavigator.navigateBack()
                        AccountNavigation.ConfirmSignOut -> onNavigate(AppNavigation.SignOut())
                        AccountNavigation.DismissDialog -> appNavigator.navigateBack()
                        AccountNavigation.SignOut -> {}
                        AccountNavigation.Subscription -> {}
                        AccountNavigation.Upgrade -> {}
                    }
                }
            )
        }

        bottomSheet(EnterPin) {
            BackHandler {
                dismissBottomSheet {
                    appNavigator.navigateBack(comesFromBottomsheet = true)
                }
            }

            EnterPinBottomsheet(
                onNavigate = {
                    when (it) {
                        EnterPinNavigation.Success -> appNavigator.navigateBack()
                        EnterPinNavigation.ForceSignOut -> onNavigate(AppNavigation.SignOut())
                    }
                }
            )
        }
    }
}
