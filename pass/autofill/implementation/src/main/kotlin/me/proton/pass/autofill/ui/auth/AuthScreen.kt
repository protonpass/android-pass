package me.proton.pass.autofill.ui.auth

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

const val AUTH_SCREEN_ROUTE = "common/auth"

@Composable
fun AuthScreen(
    onAuthSuccessful: () -> Unit,
    onAuthFailed: () -> Unit
) {
    val viewModel: AuthViewModel = hiltViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) {
        when (state) {
            AuthStatus.Success -> onAuthSuccessful()
            AuthStatus.Failed -> onAuthFailed()
            else -> {}
        }
    }

    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.init(ctx)
    }

    Text("Place your fingerprint, please")
}
