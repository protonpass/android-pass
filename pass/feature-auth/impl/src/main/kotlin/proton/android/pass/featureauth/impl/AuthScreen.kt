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
import proton.android.pass.biometry.ContextHolder

const val AUTH_SCREEN_ROUTE = "common/auth"

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
