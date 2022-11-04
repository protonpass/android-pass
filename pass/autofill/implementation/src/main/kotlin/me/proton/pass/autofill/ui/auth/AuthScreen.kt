package me.proton.pass.autofill.ui.auth

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.pass.biometry.ContextHolder
import me.proton.pass.common.api.Some
import java.lang.ref.WeakReference

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
        viewModel.init(ContextHolder(Some(WeakReference(ctx))))
    }

    when (state) {
        AuthStatus.Canceled -> {
            Text("Auth canceled by the user. TODO: Do something")
        }
        AuthStatus.Pending -> {
            Text("Place your fingerprint, please")
        }
        AuthStatus.Success -> {
            Text("Welcome back!")
        }
        AuthStatus.Failed -> {
            Text("Auth failed!")
        }
    }
}
