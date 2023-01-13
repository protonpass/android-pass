package proton.android.pass.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.composable
import proton.android.pass.pass.featurehome.impl.HomeFilterMode
import proton.android.pass.pass.featurehome.impl.HomeScreenNavigation
import proton.android.pass.pass.featurehome.impl.NavHome
import proton.android.pass.ui.navigation.AppNavItem

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.homeGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    onDrawerIconClick: () -> Unit,
    homeFilterMode: HomeFilterMode
) {
    composable(AppNavItem.Home) {
        NavHome(
            navigationDrawer = navigationDrawer,
            homeScreenNavigation = homeScreenNavigation,
            onDrawerIconClick = onDrawerIconClick,
            homeFilterMode = homeFilterMode
        )
    }
}
