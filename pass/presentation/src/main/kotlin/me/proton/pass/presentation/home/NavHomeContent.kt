package me.proton.pass.presentation.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import me.proton.pass.common.api.Some

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
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
