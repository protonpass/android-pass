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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import proton.android.pass.composecomponents.impl.bottomsheet.PassBottomSheetBackHandler
import proton.android.pass.features.home.HomeNavItem
import proton.android.pass.navigation.api.AppNavigator

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun PassNavHost(
    modifier: Modifier = Modifier,
    appNavigator: AppNavigator,
    startDestination: String = HomeNavItem.route,
    graph: NavGraphBuilder.() -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = appNavigator.navController,
        startDestination = startDestination
    ) {
        graph()
    }
    PassBottomSheetBackHandler(
        bottomSheetState = appNavigator.passBottomSheetNavigator.sheetState
    )
}

