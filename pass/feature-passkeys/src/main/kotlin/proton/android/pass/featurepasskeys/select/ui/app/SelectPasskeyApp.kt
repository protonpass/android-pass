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

package proton.android.pass.featurepasskeys.select.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.theme.SystemUIEffect
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.featurepasskeys.select.navigation.SelectPasskeyNavigation
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyAppEvent
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyAppState
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyAppViewModel

@Composable
fun SelectPasskeyApp(
    modifier: Modifier = Modifier,
    appState: SelectPasskeyAppState.Ready,
    onNavigate: (SelectPasskeyNavigation) -> Unit,
    viewModel: SelectPasskeyAppViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setInitialData(appState.data)
    }

    var selectPasskey: SelectPasskeyAppEvent.SelectPasskeyFromItem? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(state) {
        when (val event = state) {
            is SelectPasskeyAppEvent.Idle -> {}
            is SelectPasskeyAppEvent.Cancel -> {
                onNavigate(SelectPasskeyNavigation.Cancel)
            }
            is SelectPasskeyAppEvent.SelectPasskeyFromItem -> {
                selectPasskey = event
            }

            is SelectPasskeyAppEvent.SendResponse -> {
                onNavigate(SelectPasskeyNavigation.SendResponse(event.response))
            }
        }
        viewModel.clearEvent()
    }

    val isDark = isDark(appState.theme)
    SystemUIEffect(isDark = isDark)

    PassTheme(isDark = isDark) {
        Scaffold(
            modifier = modifier
                .background(PassTheme.colors.backgroundStrong)
                .systemBarsPadding()
                .imePadding()
        ) { padding ->
            SelectPasskeyAppContent(
                modifier = Modifier.padding(padding),
                needsAuth = appState.needsAuth,
                selectPasskey = selectPasskey,
                onEvent = {
                    when (it) {
                        is SelectPasskeyEvent.OnItemSelected -> {
                            viewModel.onItemSelected(
                                item = it.item,
                                origin = appState.data.domain,
                                request = appState.data.request,
                            )
                        }

                        is SelectPasskeyEvent.OnPasskeySelected -> {
                            viewModel.onPasskeySelected(
                                origin = appState.data.domain,
                                passkey = it.passkey,
                                request = appState.data.request
                            )
                        }
                    }
                },
                onNavigate = onNavigate
            )
        }
    }
}
