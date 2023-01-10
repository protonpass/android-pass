package proton.android.pass.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem
import proton.android.pass.presentation.home.HomeFilterMode
import proton.android.pass.presentation.home.HomeScreenNavigation
import proton.android.pass.presentation.home.NavHome

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
