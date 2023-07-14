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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import proton.android.pass.featureauth.impl.Auth
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.navigation.appGraph
import proton.android.pass.ui.navigation.unAuthGraph

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Suppress("LongParameterList")
fun PassNavHost(
    modifier: Modifier = Modifier,
    appNavigator: AppNavigator,
    startingRoute: String,
    onNavigate: (AppNavigation) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    AnimatedNavHost(
        modifier = modifier,
        navController = appNavigator.navController,
        startDestination = startingRoute
    ) {
        appGraph(
            appNavigator = appNavigator,
            onNavigate = onNavigate,
            dismissBottomSheet = dismissBottomSheet
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Suppress("LongParameterList")
fun PassUnAuthNavHost(
    modifier: Modifier = Modifier,
    appNavigator: AppNavigator,
    onNavigate: (AppNavigation) -> Unit
) {
    AnimatedNavHost(
        modifier = modifier,
        navController = appNavigator.navController,
        startDestination = Auth.route
    ) {
        unAuthGraph(
            appNavigator = appNavigator,
            onNavigate = onNavigate
        )
    }
}


