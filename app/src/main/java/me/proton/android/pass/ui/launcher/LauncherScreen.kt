package me.proton.android.pass.ui.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.presentation.components.common.DeferredCircularProgressIndicator
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

object LauncherScreen {
    const val route = "auth"

    @Composable
    fun view(
        navigateToHomeScreen: (userId: UserId) -> Unit
    ) {
        val viewModel = hiltViewModel<LauncherViewModel>()
        val viewState by rememberFlowWithLifecycle(viewModel.viewState())
            .collectAsState(initial = LauncherViewModel.ViewState())
        Launcher(viewState, navigateToHomeScreen)
    }
}

@Composable
internal fun Launcher(
    viewState: LauncherViewModel.ViewState,
    navigateToHomeScreen: (userId: UserId) -> Unit
) {
    when (val state = viewState.primaryAccountState) {
        is PrimaryAccountState.SignedIn -> navigateToHomeScreen(state.userId)
        else -> LoadingLauncher()
    }
}

@Composable
internal fun LoadingLauncher() {
    DeferredCircularProgressIndicator()
}
