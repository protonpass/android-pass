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

package proton.android.pass.featuresearchoptions.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet

enum class SortingLocation {
    Home,
    Autofill
}

object SortingLocationNavArgId : NavArgId {
    override val key = "sorting_location"
    override val navType = NavType.StringType
}

object EnableBulkActionsNavArgId : NavArgId {
    override val key = "bulk_actions_enabled"
    override val navType = NavType.BoolType
}

object SortingBottomsheetNavItem : NavItem(
    baseRoute = "searchoptions/sorting/bottomsheet",
    navArgIds = listOf(SortingLocationNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(location: SortingLocation): String = "$baseRoute/${location.name}"
}

object SearchOptionsBottomsheetNavItem : NavItem(
    baseRoute = "searchoptions/bottomsheet",
    navArgIds = listOf(EnableBulkActionsNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createRoute(bulkActionsEnabled: Boolean) = "$baseRoute/$bulkActionsEnabled"
}

object FilterBottomsheetNavItem : NavItem(
    baseRoute = "searchoptions/filter/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

fun NavGraphBuilder.searchOptionsGraph(onNavigateEvent: (SearchOptionsNavigation) -> Unit) {
    bottomSheet(SearchOptionsBottomsheetNavItem) {
        SearchOptionsBottomSheet(
            onNavigateEvent = onNavigateEvent
        )
    }
    bottomSheet(SortingBottomsheetNavItem) {
        SortingBottomSheet(
            onNavigateEvent = onNavigateEvent
        )
    }
    bottomSheet(FilterBottomsheetNavItem) {
        FilterBottomSheet(
            onNavigateEvent = onNavigateEvent
        )
    }
}

