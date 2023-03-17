package proton.android.pass.featurehome.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onEditVaultClick: (ShareId) -> Unit
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
                homeScreenNavigation = homeScreenNavigation,
                onAddItemClick = onAddItemClick,
                onCreateVaultClick = onCreateVaultClick,
                onEditVaultClick = onEditVaultClick
            )
        }
    }
}
