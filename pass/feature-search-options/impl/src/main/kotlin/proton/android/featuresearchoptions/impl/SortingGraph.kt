package proton.android.featuresearchoptions.impl

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.SortingTypeNavArgId
import proton.android.pass.navigation.api.bottomSheet

object SortingBottomsheet : NavItem(
    baseRoute = "sorting/bottomsheet",
    navArgIds = listOf(SortingTypeNavArgId)
) {
    fun createNavRoute(sortingType: SearchSortingType): String = buildString {
        append("$baseRoute/${sortingType.name}")
    }
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

