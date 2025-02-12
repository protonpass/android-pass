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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

sealed interface EnterPinNavigation {

    data object CloseBottomsheet : EnterPinNavigation

    data object ForceSignOutAllUsers : EnterPinNavigation

    @JvmInline
    value class Success(val origin: AuthOrigin) : EnterPinNavigation
}

@Composable
fun EnterPinBottomsheet(
    modifier: Modifier = Modifier,
    viewModel: EnterPinViewModel = hiltViewModel(),
    onNavigate: (EnterPinNavigation) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val event = (state as? EnterPinUiState.Data)?.event ?: EnterPinEvent.Unknown
    LaunchedEffect(event) {
        when (event) {
            is EnterPinEvent.ForceSignOutAllUsers -> onNavigate(EnterPinNavigation.ForceSignOutAllUsers)
            is EnterPinEvent.ForcePassword -> onNavigate(EnterPinNavigation.CloseBottomsheet)
            is EnterPinEvent.Success -> onNavigate(EnterPinNavigation.Success(event.origin))
            EnterPinEvent.Unknown -> {
            }
        }
    }

    EnterPinContent(
        modifier = modifier,
        state = state,
        onPinChanged = viewModel::onPinChanged,
        onPinSubmit = {
            keyboardController?.hide()
            viewModel.onPinSubmit()
        }
    )
}
