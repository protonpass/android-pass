package me.proton.android.pass.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.pass.ui.NavHomeUiState
import me.proton.android.pass.ui.NavHomeViewModel

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
