package me.proton.pass.presentation.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import me.proton.pass.common.api.Some

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavHomeContent(
    modifier: Modifier,
    state: NavHomeUiState,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    homeFilterMode: HomeFilterMode,
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
                    modifier = modifier,
                    homeScreenNavigation = homeScreenNavigation,
                    homeFilterMode = homeFilterMode,
                    onDrawerIconClick = onDrawerIconClick
                )
            }
        }
    }
}
