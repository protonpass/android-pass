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

package proton.android.pass.featurepasskeys.create.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.EnterPin
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featurepasskeys.create.presentation.CreatePasskeyRequest
import proton.android.pass.featurepasskeys.create.ui.app.CreatePasskeyNavigation
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

val TMP_SELECT_ITEM = NavItem(baseRoute = "tmp/select_item")

fun NavGraphBuilder.createPasskeyActivityGraph(
    appNavigator: AppNavigator,
    request: CreatePasskeyRequest,
    onNavigate: (CreatePasskeyNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    authGraph(
        canLogout = false,
        navigation = {
            when (it) {
                AuthNavigation.Back -> onNavigate(CreatePasskeyNavigation.Cancel)
                AuthNavigation.Success -> appNavigator.navigate(TMP_SELECT_ITEM)
                AuthNavigation.Dismissed -> onNavigate(CreatePasskeyNavigation.Cancel)
                AuthNavigation.Failed -> onNavigate(CreatePasskeyNavigation.Cancel)
                AuthNavigation.SignOut -> {}
                AuthNavigation.ForceSignOut -> onNavigate(CreatePasskeyNavigation.ForceSignOut)
                AuthNavigation.EnterPin -> appNavigator.navigate(EnterPin)
            }
        }
    )

    composable(TMP_SELECT_ITEM) {
        dismissBottomSheet { onNavigate(CreatePasskeyNavigation.Cancel) }
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "TMP SCREEN")
        }
    }

}
