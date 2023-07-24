/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureauth.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.toClassHolder

@Composable
fun AuthScreen(
    navigation: (AuthNavigation) -> Unit,
    canLogout: Boolean,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    LaunchedEffect(state.event) {
        when (state.event) {
            AuthEvent.Success -> navigation(AuthNavigation.Success)
            AuthEvent.Failed -> navigation(AuthNavigation.Failed)
            AuthEvent.Canceled -> navigation(AuthNavigation.Dismissed)
            AuthEvent.SignOut -> navigation(AuthNavigation.SignOut)
            AuthEvent.ForceSignOut -> navigation(AuthNavigation.ForceSignOut)
            AuthEvent.EnterPin -> navigation(AuthNavigation.EnterPin)
            AuthEvent.Unknown -> {}
        }
        viewModel.clearEvent()
    }

    LaunchedEffect(Unit) {
        viewModel.init(ctx.toClassHolder())
    }

    AuthScreenContent(
        state = state.content,
        canLogout = canLogout,
        onEvent = {
            when (it) {
                is AuthUiEvent.OnPasswordUpdate -> viewModel.onPasswordChanged(it.value)
                AuthUiEvent.OnPasswordSubmit -> viewModel.onSubmit()
                AuthUiEvent.OnSignOut -> viewModel.onSignOut()
                is AuthUiEvent.OnTogglePasswordVisibility ->
                    viewModel.onTogglePasswordVisibility(it.value)
            }
        }
    )
}
