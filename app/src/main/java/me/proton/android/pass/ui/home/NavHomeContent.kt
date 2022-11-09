package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.proton.android.pass.ui.NavHomeUiState
import me.proton.pass.common.api.Some

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavHomeContent(
    state: NavHomeUiState,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    onDrawerIconClick: () -> Unit
) {
    when {
        state.shouldAuthenticate is Some && state.shouldAuthenticate.value -> {
            LaunchedEffect(Unit) {
                homeScreenNavigation.toAuth()
            }
        }
        state.hasCompletedOnBoarding is Some && !state.hasCompletedOnBoarding.value -> {
            LaunchedEffect(Unit) {
                homeScreenNavigation.toOnBoarding()
            }
        }
        state.hasCompletedOnBoarding is Some && state.shouldAuthenticate is Some -> {
            navigationDrawer {
                HomeScreen(
                    homeScreenNavigation = homeScreenNavigation,
                    onDrawerIconClick = onDrawerIconClick
                )
            }
        }
    }
}
