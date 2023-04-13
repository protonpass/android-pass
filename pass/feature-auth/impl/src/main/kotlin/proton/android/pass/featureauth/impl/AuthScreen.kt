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
    onAuthSuccessful: () -> Unit,
    onAuthFailed: () -> Unit,
    onAuthDismissed: () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            AuthStatus.Success -> onAuthSuccessful()
            AuthStatus.Failed -> onAuthFailed()
            AuthStatus.Canceled -> onAuthDismissed()
            AuthStatus.Pending -> {}
        }
    }

    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.init(ContextHolder.fromContext(ctx))
    }

    AuthScreenContent()
}
