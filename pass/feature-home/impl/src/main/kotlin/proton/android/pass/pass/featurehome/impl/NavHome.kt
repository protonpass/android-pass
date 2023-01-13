package proton.android.pass.pass.featurehome.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun NavHome(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    homeFilterMode: HomeFilterMode,
    onDrawerIconClick: () -> Unit,
    viewModel: NavHomeViewModel = hiltViewModel()
) {
    val state by viewModel.navHomeUiState.collectAsStateWithLifecycle(NavHomeUiState.Initial)

    NavHomeContent(
        state = state,
        navigationDrawer = navigationDrawer,
        homeScreenNavigation = homeScreenNavigation,
        onDrawerIconClick = onDrawerIconClick,
        homeFilterMode = homeFilterMode
    )
}
