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

package proton.android.pass.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils.openWebsite

@Suppress("ComplexMethod")
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigate: (SettingsNavigation) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        if (state.event == SettingsEvent.RestartApp) {
            onNavigate(SettingsNavigation.Restart)
        }
    }

    LaunchedEffect(state.isForceRefreshing) {
        if (state.isForceRefreshing) {
            onNavigate(SettingsNavigation.SyncDialog)
        }
    }

    val context = LocalContext.current
    SettingsContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                is SettingsContentEvent.UseFaviconsChange -> viewModel.onUseFaviconsChange(it.value)
                is SettingsContentEvent.UseDigitalAssetLinksChange ->
                    viewModel.onUseDigitalAssetLinksChange(it.value)
                is SettingsContentEvent.AllowScreenshotsChange ->
                    viewModel.onAllowScreenshotsChange(it.value)
                is SettingsContentEvent.TelemetryChange -> viewModel.onTelemetryChange(it.value)
                is SettingsContentEvent.CrashReportChange -> viewModel.onCrashReportChange(it.value)
                SettingsContentEvent.ViewLogs -> onNavigate(SettingsNavigation.ViewLogs)
                SettingsContentEvent.ForceSync -> viewModel.onForceSync()
                SettingsContentEvent.SelectTheme -> onNavigate(SettingsNavigation.SelectTheme)
                SettingsContentEvent.Clipboard -> onNavigate(SettingsNavigation.ClipboardSettings)
                SettingsContentEvent.Privacy -> { openWebsite(context, "https://proton.me/legal/privacy") }
                SettingsContentEvent.Terms -> { openWebsite(context, "https://proton.me/legal/terms") }
                SettingsContentEvent.Up -> onNavigate(SettingsNavigation.CloseScreen)
                is SettingsContentEvent.OnDisplayUsernameToggled -> {
                    viewModel.onToggleDisplayUsernameField(isEnabled = it.isEnabled)
                }

                is SettingsContentEvent.OnDisplayAutofillPinningToggled -> {
                    viewModel.onToggleDisplayAutofillPinning(isEnabled = it.isEnabled)
                }
            }
        }
    )
}
