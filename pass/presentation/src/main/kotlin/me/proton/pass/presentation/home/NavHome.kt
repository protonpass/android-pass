package me.proton.pass.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NavHome(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    onDrawerIconClick: () -> Unit,
    viewModel: NavHomeViewModel = hiltViewModel()
) {
    val state by viewModel.navHomeUiState.collectAsStateWithLifecycle(NavHomeUiState.Initial)

    NavHomeContent(state, navigationDrawer, homeScreenNavigation, onDrawerIconClick)
}
