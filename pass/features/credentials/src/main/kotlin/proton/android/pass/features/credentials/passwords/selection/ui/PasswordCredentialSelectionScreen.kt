/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passwords.selection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonpresentation.api.snackbar.SnackbarViewModel
import proton.android.pass.commonpresentation.impl.snackbar.SnackBarViewModelImpl
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.composecomponents.impl.snackbar.SnackBarLaunchedEffect
import proton.android.pass.features.credentials.passwords.selection.navigation.PasswordCredentialSelectionNavEvent
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionEvent
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionState

@Composable
internal fun PasswordCredentialSelectionScreen(
    modifier: Modifier = Modifier,
    state: PasswordCredentialSelectionState.Ready,
    onNavigate: (PasswordCredentialSelectionNavEvent) -> Unit,
    onEvent: (PasswordCredentialSelectionEvent) -> Unit,
    snackbarViewModel: SnackbarViewModel = hiltViewModel<SnackBarViewModelImpl>()
) {
    val scaffoldState = rememberScaffoldState()
    val snackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)
    val snackbarState by snackbarViewModel.state.collectAsStateWithLifecycle()

    SnackBarLaunchedEffect(
        snackBarMessage = snackbarState.value(),
        passSnackBarHostState = snackbarHostState,
        onSnackBarMessageDelivered = { snackbarViewModel.onSnackbarMessageDelivered() }
    )

    Scaffold(
        modifier = modifier
            .background(PassTheme.colors.backgroundStrong)
            .systemBarsPadding()
            .imePadding(),
        snackbarHost = {
            PassSnackbarHost(snackbarHostState = snackbarHostState)
        }
    ) { innerPaddingValues ->
        PasswordCredentialSelectionContent(
            modifier = Modifier.padding(paddingValues = innerPaddingValues),
            state = state,
            onNavigate = onNavigate,
            onEvent = onEvent
        )
    }
}
