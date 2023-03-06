package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.Option
import proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun NavHome(
    navigationDrawer: @Composable (@Composable () -> Unit) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    homeItemTypeSelection: HomeItemTypeSelection,
    homeVaultSelection: HomeVaultSelection,
    onDrawerIconClick: () -> Unit,
    onAddItemClick: (Option<ShareId>) -> Unit,
    viewModel: NavHomeViewModel = hiltViewModel()
) {
    val state by viewModel.navHomeUiState.collectAsStateWithLifecycle(NavHomeUiState.Initial)

    NavHomeContent(
        state = state,
        navigationDrawer = navigationDrawer,
        homeScreenNavigation = homeScreenNavigation,
        onDrawerIconClick = onDrawerIconClick,
        homeItemTypeSelection = homeItemTypeSelection,
        homeVaultSelection = homeVaultSelection,
        onAddItemClick = onAddItemClick
    )
}
