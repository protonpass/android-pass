/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.explore.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.features.explore.ui.ExploreScreen
import proton.android.pass.features.explore.ui.codes.CodesScreen
import proton.android.pass.navigation.api.composable

fun NavGraphBuilder.exploreNavGraph(onNavigated: (ExploreNavDestination) -> Unit) {
    composable(navItem = ExploreNavItem) {
        val context = LocalContext.current
        ExploreScreen(
            onNavigated = { destination ->
                when (destination) {
                    is ExploreNavDestination.ExternalUrl ->
                        BrowserUtils.openWebsite(context, destination.url)
                    else -> onNavigated(destination)
                }
            }
        )
    }
    composable(navItem = CodesNavItem) {
        CodesScreen(onUpClick = { onNavigated(ExploreNavDestination.CodesBack) })
    }
}
