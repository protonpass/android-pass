/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.autofill.autofillhealth.troubleshooting.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillTroubleshootingViewModel
import proton.android.pass.commonui.api.BrowserUtils

@Composable
fun AutofillTroubleshootingScreen(
    onBack: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutofillTroubleshootingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    AutofillTroubleshootingContent(
        modifier = modifier,
        state = state,
        onEvent = { event ->
            when (event) {
                AutofillTroubleshootingUiEvent.OnBack -> onBack()
                AutofillTroubleshootingUiEvent.OnEnableAutofill -> viewModel.openAutofillSelector()
                is AutofillTroubleshootingUiEvent.OnOpenBrowser ->
                    BrowserUtils.openApp(context, event.browser.packageName)
                AutofillTroubleshootingUiEvent.OnShareDiagnostics ->
                    AutofillTroubleshootingActions.shareDiagnostics(context, state)
                AutofillTroubleshootingUiEvent.OnOpenDiagnostics -> onNavigateToDiagnostics()
            }
        }
    )
}
