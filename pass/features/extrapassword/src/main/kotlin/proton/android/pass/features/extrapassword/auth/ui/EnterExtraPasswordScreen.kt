/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.extrapassword.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.domain.entity.UserId
import proton.android.pass.features.extrapassword.auth.presentation.EnterExtraPasswordEvent
import proton.android.pass.features.extrapassword.auth.presentation.EnterExtraPasswordViewModel

@Composable
fun EnterExtraPasswordScreen(
    modifier: Modifier = Modifier,
    userId: UserId?,
    onSuccess: () -> Unit,
    onLogout: (UserId) -> Unit,
    viewModel: EnterExtraPasswordViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.setUserId(userId)
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.event) {
        when (val event = state.event) {
            EnterExtraPasswordEvent.Idle -> {}
            EnterExtraPasswordEvent.Success -> onSuccess()
            is EnterExtraPasswordEvent.Logout -> onLogout(event.userId)
        }
        viewModel.consumeEvent(state.event)
    }

    EnterAccessKeyContent(
        modifier = modifier,
        state = state,
        content = viewModel.extraPasswordState,
        onValueChange = viewModel::onExtraPasswordChanged,
        onSubmit = viewModel::onSubmit
    )
}
