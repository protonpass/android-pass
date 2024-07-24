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

package proton.android.pass.features.sl.sync.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.sl.sync.settings.presentation.SimpleLoginSyncSettingsState

@Composable
internal fun SimpleLoginSyncSettingsContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SimpleLoginSyncUiEvent) -> Unit,
    state: SimpleLoginSyncSettingsState
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SimpleLoginSyncSettingsTopBar(
                onUpClick = {
                    onUiEvent(SimpleLoginSyncUiEvent.OnCloseClicked)
                },
                onConfirmClick = {
                    onUiEvent(SimpleLoginSyncUiEvent.OnConfirmClicked)
                }
            )
        }
    ) { innerPaddingValue ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPaddingValue)
                .padding(all = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            if (hasVaults) {
                SimpleLoginSyncSettingsVault(
                    selectedVault = selectedVault
                )
            }

            SimpleLoginSyncSettingsNotes(
                isChecked = isNotesStoringEnabled,
                onCheckedChange = { isChecked ->
                    SimpleLoginSyncUiEvent.OnNoteStoringFlagChanged(
                        isEnabled = isChecked
                    ).also(onUiEvent)
                },
                onLinkClick = { onUiEvent(SimpleLoginSyncUiEvent.OnLinkClicked) }
            )
        }
    }
}
