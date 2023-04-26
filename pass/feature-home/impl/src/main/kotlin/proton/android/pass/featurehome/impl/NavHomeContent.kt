package proton.android.pass.featurehome.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import proton.android.pass.common.api.Some

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavHomeContent(
    state: NavHomeUiState,
    onNavigateEvent: (HomeNavigation) -> Unit
) {
    when {
        state.shouldAuthenticate is Some && state.shouldAuthenticate.value -> {
            LaunchedEffect(Unit) {
                onNavigateEvent(HomeNavigation.Auth)
            }
        }

        state.hasCompletedOnBoarding is Some && !state.hasCompletedOnBoarding.value -> {
            LaunchedEffect(Unit) {
                onNavigateEvent(HomeNavigation.OnBoarding)
            }
        }

        state.hasCompletedOnBoarding is Some && state.shouldAuthenticate is Some -> {
            HomeScreen(
                modifier = Modifier.testTag(HomeScreenTestTag.screen),
                onNavigateEvent = onNavigateEvent
            )
        }
    }
}
