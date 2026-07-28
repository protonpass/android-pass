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

package proton.android.pass.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
import proton.android.pass.composecomponents.impl.setting.SettingToggle

@Composable
internal fun OfflineAttachmentsSection(
    modifier: Modifier = Modifier,
    isDownloadAllEnabled: Boolean,
    isSharedItemsEnabled: Boolean,
    isAllowCellularEnabled: Boolean,
    isDownloadEnabled: Boolean,
    isPaidFeature: Boolean,
    onEvent: (SettingsContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Text(
            text = stringResource(R.string.settings_offline_attachments_section_title),
            style = ProtonTheme.typography.defaultSmallWeak
        )

        Column(modifier = Modifier.roundedContainerNorm()) {
            if (isPaidFeature) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(SettingsContentEvent.OnOfflineAttachmentsUpsell) }
                        .padding(horizontal = 16.dp, vertical = 26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_offline_attachments_download_all),
                        style = ProtonTheme.typography.defaultNorm,
                        color = PassTheme.colors.textNorm
                    )
                    PassPlusIcon()
                }
            } else {
                SettingToggle(
                    text = stringResource(R.string.settings_offline_attachments_download_all),
                    isChecked = isDownloadAllEnabled,
                    onClick = { newIsEnabled ->
                        onEvent(SettingsContentEvent.OnDownloadAllAttachmentsToggled(newIsEnabled))
                    }
                )
            }

            if (isDownloadAllEnabled && !isPaidFeature) {
                PassDivider()
                SettingToggle(
                    text = stringResource(R.string.settings_offline_attachments_shared_items),
                    isChecked = isSharedItemsEnabled,
                    onClick = { newIsEnabled ->
                        onEvent(SettingsContentEvent.OnSharedItemsToggled(newIsEnabled))
                    }
                )

                PassDivider()
                SettingToggle(
                    text = stringResource(R.string.settings_offline_attachments_cellular),
                    isChecked = isAllowCellularEnabled,
                    onClick = { newIsEnabled ->
                        onEvent(SettingsContentEvent.OnAllowCellularDownloadToggled(newIsEnabled))
                    }
                )

                if (isDownloadEnabled) {
                    PassDivider()
                    SettingOption(
                        text = stringResource(R.string.settings_offline_attachments_download_status),
                        onClick = { onEvent(SettingsContentEvent.OnOpenDownloadStatus) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun OfflineAttachmentsToggleOffPreview() {
    PassTheme(isDark = false) {
        Surface {
            OfflineAttachmentsSection(
                isDownloadAllEnabled = false,
                isSharedItemsEnabled = true,
                isAllowCellularEnabled = false,
                isDownloadEnabled = false,
                onEvent = {},
                isPaidFeature = false
            )
        }
    }
}

@Preview
@Composable
internal fun OfflineAttachmentsToggleOnPreview() {
    PassTheme(isDark = true) {
        Surface {
            OfflineAttachmentsSection(
                isDownloadAllEnabled = true,
                isSharedItemsEnabled = true,
                isAllowCellularEnabled = true,
                isDownloadEnabled = false,
                isPaidFeature = false,
                onEvent = {}
            )
        }
    }
}

@Preview
@Composable
internal fun OfflineAttachmentsInProgressPreview() {
    PassTheme(isDark = false) {
        Surface {
            OfflineAttachmentsSection(
                isDownloadAllEnabled = true,
                isSharedItemsEnabled = false,
                isAllowCellularEnabled = false,
                isDownloadEnabled = true,
                isPaidFeature = false,
                onEvent = {}
            )
        }
    }
}

@Preview
@Composable
internal fun OfflineAttachmentsUpsellPreview() {
    PassTheme(isDark = false) {
        Surface {
            OfflineAttachmentsSection(
                isDownloadAllEnabled = false,
                isSharedItemsEnabled = false,
                isAllowCellularEnabled = false,
                isDownloadEnabled = false,
                isPaidFeature = true,
                onEvent = {}
            )
        }
    }
}
