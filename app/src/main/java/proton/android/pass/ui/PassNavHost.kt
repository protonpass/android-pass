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

package proton.android.pass.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.navigation.UN_AUTH_GRAPH
import proton.android.pass.ui.navigation.appGraph
import proton.android.pass.ui.navigation.unAuthGraph

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun PassNavHost(
    modifier: Modifier = Modifier,
    appNavigator: AppNavigator,
    onNavigate: (AppNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = appNavigator.navController,
        startDestination = Home.route
    ) {
        appGraph(
            appNavigator = appNavigator,
            onNavigate = onNavigate,
            dismissBottomSheet = dismissBottomSheet
        )
    }
}

@Composable
@Suppress("LongParameterList")
fun PassUnAuthNavHost(
    modifier: Modifier = Modifier,
    appNavigator: AppNavigator,
    onNavigate: (AppNavigation) -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = appNavigator.navController,
        startDestination = UN_AUTH_GRAPH
    ) {
        unAuthGraph(
            appNavigator = appNavigator,
            onNavigate = onNavigate,
        )
    }
}


