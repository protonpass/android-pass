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

package proton.android.pass.features.passkeys.create.ui.app

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
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyAppEvent
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyAppState
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyAppViewModel
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyNavState
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyRequest
import proton.android.pass.features.passkeys.create.ui.confirm.ConfirmItemDialog

@Composable
fun CreatePasskeyApp(
    modifier: Modifier = Modifier,
    appState: CreatePasskeyAppState.Ready,
    request: CreatePasskeyRequest,
    onNavigate: (CreatePasskeyNavigation) -> Unit,
    viewModel: CreatePasskeyAppViewModel = hiltViewModel(),
    snackbarViewModel: SnackbarViewModel = hiltViewModel<SnackBarViewModelImpl>()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    val snackbarState by snackbarViewModel.state.collectAsStateWithLifecycle()
    SnackBarLaunchedEffect(
        snackBarMessage = snackbarState.value(),
        passSnackBarHostState = passSnackbarHostState,
        onSnackBarMessageDelivered = { snackbarViewModel.onSnackbarMessageDelivered() }
    )

    LaunchedEffect(Unit) {
        viewModel.setInitialData(request, appState)
    }

    var askForConfirmation: CreatePasskeyAppEvent.AskForConfirmation? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(state.event) {
        when (val event = state.event) {
            is CreatePasskeyAppEvent.Idle -> {}
            is CreatePasskeyAppEvent.AskForConfirmation -> {
                askForConfirmation = event
            }

            is CreatePasskeyAppEvent.SendResponse -> {
                askForConfirmation = null
                onNavigate(CreatePasskeyNavigation.SendResponse(event.response))
            }
        }
        viewModel.clearEvent()
    }

    when (val navState = state.navState) {
        CreatePasskeyNavState.Loading -> {}
        is CreatePasskeyNavState.Ready -> {
            Scaffold(
                modifier = modifier
                    .background(PassTheme.colors.backgroundStrong)
                    .systemBarsPadding()
                    .imePadding(),
                snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
            ) { padding ->
                CreatePasskeyAppContent(
                    modifier = Modifier.padding(padding),
                    needsAuth = appState.needsAuth,
                    navState = navState,
                    onEvent = {
                        when (it) {
                            is CreatePasskeyEvent.OnItemSelected -> {
                                viewModel.onItemSelected(it.item)
                            }
                        }
                    },
                    onNavigate = onNavigate
                )

                askForConfirmation?.let { event ->
                    ConfirmItemDialog(
                        item = event.item,
                        isLoading = event.isLoadingState,
                        onConfirm = {
                            viewModel.onConfirmed(event.item, request)
                        },
                        onDismiss = {
                            askForConfirmation = null
                        }
                    )
                }
            }
        }
    }
}
