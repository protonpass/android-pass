/*
 * Copyright (c) 2023-2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.auth

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.toClassHolder

@Suppress("ComplexMethod")
@Composable
fun AuthScreen(
    navigation: (AuthNavigation) -> Unit,
    canLogout: Boolean,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    BackHandler { navigation(AuthNavigation.CloseScreen(viewModel.origin)) }
    LaunchedEffect(state.event) {
        when (val authEventOption = state.event) {
            None -> viewModel.onAuthMethodRequested()
            is Some -> {
                when (val event = authEventOption.value) {
                    is AuthEvent.Success -> navigation(AuthNavigation.Success(event.origin))
                    AuthEvent.Failed -> navigation(AuthNavigation.Failed)
                    AuthEvent.Canceled -> navigation(AuthNavigation.Dismissed)
                    is AuthEvent.SignOut -> navigation(AuthNavigation.SignOut(event.userId))
                    is AuthEvent.ForceSignOut -> navigation(AuthNavigation.ForceSignOut(event.userId))
                    is AuthEvent.EnterPin -> navigation(AuthNavigation.EnterPin(event.origin))
                    AuthEvent.EnterBiometrics -> viewModel.onBiometricsRequired(ctx.toClassHolder())
                    AuthEvent.Unknown -> return@LaunchedEffect
                }
                viewModel.clearEvent()
            }
        }
    }

    AuthContent(
        state = state.content,
        canLogout = canLogout,
        onEvent = {
            when (it) {
                is AuthUiEvent.OnPasswordUpdate -> viewModel.onPasswordChanged(it.value)
                is AuthUiEvent.OnPasswordSubmit -> viewModel.onSubmit(it.value)
                AuthUiEvent.OnSignOut -> viewModel.onSignOut()
                is AuthUiEvent.OnTogglePasswordVisibility ->
                    viewModel.onTogglePasswordVisibility(it.value)

                AuthUiEvent.OnAuthAgainClick -> viewModel.onAuthMethodRequested()
                AuthUiEvent.OnNavigateBack -> navigation(AuthNavigation.CloseScreen(viewModel.origin))
                is AuthUiEvent.OnAccountSwitch -> viewModel.onAccountSwitch(it.userId)
            }
        }
    )
}
