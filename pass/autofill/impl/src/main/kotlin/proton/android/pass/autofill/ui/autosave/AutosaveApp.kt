/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.autofill.ui.autosave

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonpresentation.api.snackbar.SnackbarViewModel
import proton.android.pass.commonpresentation.impl.snackbar.SnackBarViewModelImpl
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.composecomponents.impl.snackbar.SnackBarLaunchedEffect

@Composable
fun AutoSaveApp(
    modifier: Modifier = Modifier,
    arguments: AutoSaveArguments,
    onNavigate: (AutosaveNavigation) -> Unit,
    viewModel: AutoSaveAppViewModel = hiltViewModel(),
    snackbarViewModel: SnackbarViewModel = hiltViewModel<SnackBarViewModelImpl>()
) {
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    val snackbarState by snackbarViewModel.state.collectAsStateWithLifecycle()
    SnackBarLaunchedEffect(
        snackBarMessage = snackbarState.value(),
        passSnackBarHostState = passSnackbarHostState,
        onSnackBarMessageDelivered = { snackbarViewModel.onSnackbarMessageDelivered() }
    )

    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .background(PassTheme.colors.backgroundStrong)
            .imePadding(),
        snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
    ) { padding ->
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn().togetherWith(fadeOut())
            }
        ) { state ->

            when (state) {
                is AutoSaveAppViewState.Ready -> {
                    AutosaveAppContent(
                        modifier = Modifier.padding(padding),
                        arguments = arguments,
                        state = state,
                        onItemSelectedForUpdate = viewModel::onItemSelectedForUpdate,
                        onClearSelectedItemForUpdate = viewModel::onClearSelectedItemForUpdate,
                        onNavigate = {
                            if (it == AutosaveNavigation.Success) {
                                viewModel.onItemAutoSaved()
                            }
                            onNavigate(it)
                        }
                    )
                }

                is AutoSaveAppViewState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
