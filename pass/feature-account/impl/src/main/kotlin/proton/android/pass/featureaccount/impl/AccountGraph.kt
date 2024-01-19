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

package proton.android.pass.featureaccount.impl

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog

private const val ACCOUNT_GRAPH = "account_graph"

object Account : NavItem(baseRoute = "account/view")
object SignOutDialog : NavItem(
    baseRoute = "account/signout/dialog",
    navItemType = NavItemType.Dialog
)

fun NavGraphBuilder.accountGraph(
    onNavigate: (AccountNavigation) -> Unit
) {
    navigation(
        route = ACCOUNT_GRAPH,
        startDestination = Account.route
    ) {
        composable(Account) {
            AccountScreen(
                modifier = Modifier.testTag(AccountScreenTestTag.screen),
                onNavigate = onNavigate
            )
        }
        dialog(SignOutDialog) {
            ConfirmSignOutDialog(
                onNavigate = onNavigate
            )
        }
    }
}

sealed interface AccountNavigation {
    object Subscription : AccountNavigation
    object Upgrade : AccountNavigation
    object SignOut : AccountNavigation
    object ConfirmSignOut : AccountNavigation
    object DismissDialog : AccountNavigation
    object Back : AccountNavigation
}
