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

package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.preferences.value

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onEvent: (SettingsContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.title_settings),
                onUpClick = { onEvent(SettingsContentEvent.Up) }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(PassTheme.colors.backgroundStrong)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PreferencesSection(
                theme = state.themePreference,
                onEvent = onEvent,
            )
            PrivacySection(
                useFavicons = state.useFavicons.value(),
                allowScreenshots = state.allowScreenshots.value(),
                onEvent = onEvent
            )
            DefaultVaultSection(
                defaultVault = state.defaultVault.map { it.vault },
                onEvent = onEvent
            )
            AboutSection(
                onEvent = onEvent
            )
            ApplicationSection(
                shareTelemetry = state.shareTelemetry,
                shareCrashes = state.shareCrashes,
                onEvent = onEvent
            )
        }
    }
}
