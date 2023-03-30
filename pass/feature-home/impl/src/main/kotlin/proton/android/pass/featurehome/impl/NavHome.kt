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
    onAddItemClick: (Option<ShareId>) -> Unit,
    homeScreenNavigation: HomeScreenNavigation,
    onCreateVaultClick: () -> Unit,
    onEditVaultClick: (ShareId) -> Unit,
    onDeleteVaultClick: (ShareId) -> Unit,
    viewModel: NavHomeViewModel = hiltViewModel()
) {
    val state by viewModel.navHomeUiState.collectAsStateWithLifecycle(NavHomeUiState.Initial)

    NavHomeContent(
        state = state,
        homeScreenNavigation = homeScreenNavigation,
        onAddItemClick = onAddItemClick,
        onCreateVaultClick = onCreateVaultClick,
        onEditVaultClick = onEditVaultClick,
        onDeleteVaultClick = onDeleteVaultClick
    )
}
