package proton.android.pass.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object Root : NavItem(baseRoute = "root", isTopLevel = true, noHistory = true)

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.rootGraph(
    onNavigateEvent: (RootNavigation) -> Unit,
) {
    composable(Root) {
        val viewModel: RootViewModel = hiltViewModel()
        val route = viewModel.getRoute()
        LaunchedEffect(route) {
            when (route) {
                RootNavigation.Auth -> onNavigateEvent(RootNavigation.Auth)
                RootNavigation.Home -> onNavigateEvent(RootNavigation.Home)
                RootNavigation.OnBoarding -> onNavigateEvent(RootNavigation.OnBoarding)
            }
        }
    }
}

sealed interface RootNavigation {
    object Auth : RootNavigation
    object OnBoarding : RootNavigation
    object Home : RootNavigation
}
