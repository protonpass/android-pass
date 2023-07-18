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

package proton.android.featuresearchoptions.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.SortingTypeNavArgId
import proton.android.pass.navigation.api.bottomSheet

enum class SortingLocation {
    Home,
    Autofill;
}

object SortingLocationNavArgId : NavArgId {
    override val key = "sortingLocation"
    override val navType = NavType.StringType
}

object SortingBottomsheet : NavItem(
    baseRoute = "sorting/bottomsheet",
    navArgIds = listOf(SortingTypeNavArgId, SortingLocationNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(sortingType: SearchSortingType, location: SortingLocation): String =
        "$baseRoute/${sortingType.name}/${location.name}"

}

fun NavGraphBuilder.sortingGraph(
    onNavigateEvent: (SortingNavigation) -> Unit
) {
    bottomSheet(SortingBottomsheet) {
        SortingBottomSheet(
            onNavigateEvent = onNavigateEvent
        )
    }
}

