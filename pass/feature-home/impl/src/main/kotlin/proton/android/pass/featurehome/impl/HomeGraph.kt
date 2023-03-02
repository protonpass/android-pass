package proton.android.pass.featurehome.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Home : NavItem(baseRoute = "home", isTopLevel = true)

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.homeGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    onDrawerIconClick: () -> Unit,
    homeItemTypeSelection: HomeItemTypeSelection,
    homeVaultSelection: HomeVaultSelection
) {
    composable(Home) {
        NavHome(
            navigationDrawer = navigationDrawer,
            homeScreenNavigation = homeScreenNavigation,
            onDrawerIconClick = onDrawerIconClick,
            homeItemTypeSelection = homeItemTypeSelection,
            homeVaultSelection = homeVaultSelection
        )
    }
}
