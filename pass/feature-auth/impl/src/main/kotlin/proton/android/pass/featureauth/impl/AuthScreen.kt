package proton.android.pass.featureauth.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.biometry.ContextHolder

const val AUTH_SCREEN_ROUTE = "common/auth"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AuthScreen(
    navigation: (AuthNavigation) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            AuthStatus.Success -> { navigation(AuthNavigation.Success) }
            AuthStatus.Failed -> { navigation(AuthNavigation.Failed) }
            AuthStatus.Canceled -> { navigation(AuthNavigation.Dismissed) }
            AuthStatus.Pending -> {}
        }
    }

    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.init(ContextHolder.fromContext(ctx))
    }

    AuthScreenContent()
}
