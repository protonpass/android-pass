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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.settings.presentation.SimpleLoginSyncSettingsState
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncSectionRow

@Composable
internal fun SimpleLoginSyncSettingsContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SimpleLoginSettingsSyncUiEvent) -> Unit,
    state: SimpleLoginSyncSettingsState
) = with(state) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            SimpleLoginSyncSettingsTopBar(
                onUpClick = {
                    onUiEvent(SimpleLoginSettingsSyncUiEvent.OnCloseClicked)
                },
                onConfirmClick = {
                    onUiEvent(SimpleLoginSettingsSyncUiEvent.OnConfirmClicked)
                },
                isConfirmEnabled = canConfirmSettings,
                isLoading = isEnablingSync
            )
        }
    ) { innerPaddingValue ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPaddingValue)
                .padding(all = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            if (selectedVaultOption is Some) {
                with(selectedVaultOption.value) {
                    SimpleLoginSyncSectionRow(
                        leadingIcon = {
                            VaultIcon(
                                backgroundColor = color.toColor(isBackground = true),
                                icon = icon.toResource(),
                                iconColor = color.toColor()
                            )
                        },
                        title = stringResource(id = R.string.simple_login_sync_shared_default_vault_title),
                        subtitle = name,
                        description = stringResource(id = R.string.simple_login_sync_shared_default_vault_description),
                        isClickable = !isEnablingSync,
                        onClick = {
                            SimpleLoginSettingsSyncUiEvent.OnSelectVaultClicked(
                                shareId = selectedVaultOption.value.shareId
                            ).also(onUiEvent)
                        }
                    )
                }
            }
        }
    }
}
