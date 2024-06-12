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

package proton.android.pass.features.secure.links.shared.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.secure.links.create.navigation.SecureLinksCreateNavItem
import proton.android.pass.features.secure.links.create.ui.SecureLinksCreateScreen
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewNavItem
import proton.android.pass.features.secure.links.overview.ui.SecureLinksOverviewScreen
import proton.android.pass.navigation.api.composable

fun NavGraphBuilder.secureLinksNavGraph(onNavigated: (SecureLinksNavDestination) -> Unit) {

    composable(navItem = SecureLinksCreateNavItem) {
        SecureLinksCreateScreen(onNavigated = onNavigated)
    }

    composable(navItem = SecureLinksOverviewNavItem) {
        SecureLinksOverviewScreen(onNavigated = onNavigated)
    }

}
