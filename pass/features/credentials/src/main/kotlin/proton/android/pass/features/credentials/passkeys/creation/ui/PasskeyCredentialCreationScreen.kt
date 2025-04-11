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

package proton.android.pass.features.credentials.passkeys.creation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonpresentation.api.snackbar.SnackbarViewModel
import proton.android.pass.commonpresentation.impl.snackbar.SnackBarViewModelImpl
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.composecomponents.impl.snackbar.SnackBarLaunchedEffect
import proton.android.pass.features.credentials.passkeys.creation.navigation.PasskeyCredentialCreationNavEvent
import proton.android.pass.features.credentials.passkeys.creation.presentation.PasskeyCredentialCreationEvent
import proton.android.pass.features.credentials.passkeys.creation.presentation.PasskeyCredentialCreationState
import proton.android.pass.features.credentials.passkeys.creation.presentation.PasskeyCredentialCreationStateEvent
import proton.android.pass.features.passkeys.create.ui.confirm.ConfirmItemDialog

@Composable
internal fun PasskeyCredentialCreationScreen(
    modifier: Modifier = Modifier,
    state: PasskeyCredentialCreationState.Ready,
    onNavigate: (PasskeyCredentialCreationNavEvent) -> Unit,
    onEvent: (PasskeyCredentialCreationEvent) -> Unit,
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

    var askForConfirmationEvent: PasskeyCredentialCreationStateEvent.OnAskForConfirmation? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(key1 = state.event) {
        when (val event = state.event) {
            PasskeyCredentialCreationStateEvent.Idle -> Unit
            is PasskeyCredentialCreationStateEvent.OnAskForConfirmation -> {
                askForConfirmationEvent = event
            }

            is PasskeyCredentialCreationStateEvent.OnSendResponse -> {
                askForConfirmationEvent = null

                PasskeyCredentialCreationNavEvent.SendResponse(
                    response = event.response
                ).also(onNavigate)
            }
        }

        PasskeyCredentialCreationEvent.OnEventConsumed(
            event = state.event
        ).also(onEvent)
    }

    Scaffold(
        modifier = modifier
            .background(PassTheme.colors.backgroundStrong)
            .systemBarsPadding()
            .imePadding(),
        snackbarHost = {
            PassSnackbarHost(snackbarHostState = snackbarHostState)
        }
    ) { innerPaddingValues ->
        PasskeyCredentialCreationContent(
            modifier = Modifier.padding(paddingValues = innerPaddingValues),
            state = state,
            onNavigate = onNavigate,
            onEvent = onEvent
        )

        askForConfirmationEvent?.let { event ->
            ConfirmItemDialog(
                item = event.itemUiModel,
                isLoading = event.isLoadingState,
                onConfirm = {
                    PasskeyCredentialCreationEvent.OnItemSelectionConfirmed(
                        itemUiModel = event.itemUiModel
                    ).also(onEvent)
                },
                onDismiss = {
                    askForConfirmationEvent = null
                }
            )
        }
    }
}
