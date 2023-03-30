package proton.android.pass.featurehome.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.pass.domain.ShareId

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavHomeContent(
    state: NavHomeUiState,
    homeScreenNavigation: HomeScreenNavigation,
    onAddItemClick: (Option<ShareId>) -> Unit,
    onCreateVaultClick: () -> Unit,
    onEditVaultClick: (ShareId) -> Unit,
    onDeleteVaultClick: (ShareId) -> Unit
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
            HomeScreen(
                modifier = Modifier.testTag(HomeScreenTestTag.screen),
                homeScreenNavigation = homeScreenNavigation,
                onAddItemClick = onAddItemClick,
                onCreateVaultClick = onCreateVaultClick,
                onEditVaultClick = onEditVaultClick,
                onDeleteVaultClick = onDeleteVaultClick
            )
        }
    }
}
