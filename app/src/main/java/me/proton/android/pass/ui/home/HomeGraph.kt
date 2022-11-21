package me.proton.android.pass.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.home.HomeScreenNavigation
import me.proton.pass.presentation.home.NavHome

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.homeGraph(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    onDrawerIconClick: () -> Unit
) {
    composable(AppNavItem.Home) {
        NavHome(navigationDrawer, homeScreenNavigation, onDrawerIconClick)
    }
}
