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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

sealed interface EnterPinNavigation {
    object ForceSignOut : EnterPinNavigation
    object Success : EnterPinNavigation
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EnterPinBottomsheet(
    modifier: Modifier = Modifier,
    viewModel: EnterPinViewModel = hiltViewModel(),
    onNavigate: (EnterPinNavigation) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventLaunchedEffect(state as? EnterPinUiState.Data, onNavigate)
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

@Composable
fun EventLaunchedEffect(data: EnterPinUiState.Data?, onNavigate: (EnterPinNavigation) -> Unit) {
    LaunchedEffect(data) {
        when (data?.event) {
            EnterPinEvent.ForceSignOut -> onNavigate(EnterPinNavigation.ForceSignOut)
            EnterPinEvent.Success -> onNavigate(EnterPinNavigation.Success)
            EnterPinEvent.Unknown,
            null -> {
            }
        }
    }
}
