package proton.android.pass.featurehome.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import proton.android.pass.common.api.Some

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavHomeContent(
    state: NavHomeUiState,
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    homeItemTypeSelection: HomeItemTypeSelection,
    homeVaultSelection: HomeVaultSelection,
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
                    homeItemTypeSelection = homeItemTypeSelection,
                    homeVaultSelection = homeVaultSelection,
                    onDrawerIconClick = onDrawerIconClick
                )
            }
        }
    }
}
