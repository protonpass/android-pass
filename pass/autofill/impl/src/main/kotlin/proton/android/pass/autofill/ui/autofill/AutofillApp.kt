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

package proton.android.pass.autofill.ui.autofill

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
import proton.android.pass.autofill.ui.SnackBarLaunchedEffect
import proton.android.pass.autofill.ui.SnackBarViewModel
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.composecomponents.impl.theme.SystemUIEffect
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.preferences.ThemePreference

@Composable
fun AutofillApp(
    modifier: Modifier = Modifier,
    autofillUiState: AutofillUiState.StartAutofillUiState,
    onNavigate: (AutofillNavigation) -> Unit,
    snackBarViewModel: SnackBarViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    val snackbarState by snackBarViewModel.state.collectAsStateWithLifecycle()

    SnackBarLaunchedEffect(
        snackBarMessage = snackbarState.value(),
        passSnackBarHostState = passSnackbarHostState,
        onSnackBarMessageDelivered = { snackBarViewModel.onSnackbarMessageDelivered() }
    )

    val isDark = isDark(ThemePreference.from(autofillUiState.themePreference))

    SystemUIEffect(isDark = isDark)

    PassTheme(isDark = isDark) {
        Scaffold(
            modifier = modifier
                .background(PassTheme.colors.backgroundStrong)
                .systemBarsPadding()
                .imePadding(),
            snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
        ) { padding ->
            AutofillAppContent(
                modifier = Modifier.padding(padding),
                autofillAppState = autofillUiState.autofillAppState,
                selectedAutofillItem = autofillUiState.selectedAutofillItem.value(),
                needsAuth = autofillUiState.needsAuth,
                onNavigate = {
                    snackBarViewModel.onSnackbarMessageDelivered()
                    onNavigate(it)
                }
            )
        }
    }
}

