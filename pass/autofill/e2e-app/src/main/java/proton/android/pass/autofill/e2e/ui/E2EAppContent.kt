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

package proton.android.pass.autofill.e2e.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import proton.android.pass.autofill.e2e.ui.main.MAIN_ROUTE

@Composable
fun E2EAppContent(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            E2ENavHost(
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun E2ENavHost(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = MAIN_ROUTE
    ) {
        e2eAppGraph(navigator = navController)
    }
}
