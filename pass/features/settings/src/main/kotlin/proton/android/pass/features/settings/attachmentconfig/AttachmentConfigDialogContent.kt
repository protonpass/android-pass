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

package proton.android.pass.features.settings.attachmentconfig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonAlertDialog
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.features.settings.R
import java.util.Locale
import me.proton.core.presentation.R as CoreR

@Composable
internal fun AttachmentConfigDialogContent(
    modifier: Modifier = Modifier,
    state: AttachmentConfigState,
    onAllVaultsToggled: (Boolean) -> Unit,
    onVaultToggled: (ShareId, Boolean) -> Unit,
    onSharedItemsToggled: (Boolean) -> Unit,
    onCellularToggled: (Boolean) -> Unit,
    onStartClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ProtonAlertDialog(
        modifier = modifier
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * ALERT_DIALOG_HEIGHT_FRACTION),
        onDismissRequest = onDismiss,
        title = {
            Text.Headline(
                text = stringResource(R.string.attachment_config_dialog_title)
            )
        },
        text = {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
            Column(
                modifier = Modifier.focusRequester(focusRequester).focusable(),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                ToggleRow(
                    text = stringResource(R.string.attachment_config_dialog_all_vaults).let { base ->
                        state.totalSizeBytes?.let { "$base (${formatFileSize(it)})" } ?: base
                    },
                    isChecked = state.allVaultsEnabled,
                    onToggle = onAllVaultsToggled
                )

                AnimatedVisibility(
                    visible = !state.allVaultsEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Text.Body2Weak(
                            text = stringResource(
                                R.string.attachment_config_dialog_select_vaults
                            )
                        )

                        LazyColumn(
                            modifier = Modifier.heightIn(
                                max = LocalConfiguration.current.screenHeightDp.dp * VAULT_LIST_HEIGHT_FRACTION
                            ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            items(
                                items = state.vaults,
                                key = { it.shareId.id }
                            ) { vault ->
                                VaultToggleRow(
                                    vault = vault,
                                    onToggle = { enabled ->
                                        onVaultToggled(vault.shareId, enabled)
                                    }
                                )
                            }
                        }

                        SharedItemsToggleRow(
                            text = stringResource(
                                R.string.attachment_config_dialog_shared_items
                            ),
                            isChecked = state.sharedItemsEnabled,
                            onToggle = onSharedItemsToggled
                        )

                        Divider()
                    }
                }

                ToggleRow(
                    text = stringResource(R.string.settings_offline_attachments_cellular),
                    isChecked = state.allowCellular,
                    onToggle = onCellularToggled
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onStartClick,
                enabled = state.canStartDownload,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PassTheme.colors.interactionNormMajor1,
                    disabledBackgroundColor = PassTheme.colors.interactionNormMajor1.copy(alpha = 0.5f)
                )
            ) {
                Text.Body1Regular(
                    text = stringResource(R.string.attachment_config_dialog_start),
                    color = if (state.canStartDownload) {
                        PassTheme.colors.textInvert
                    } else {
                        PassTheme.colors.textDisabled
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text.Body1Regular(
                    text = stringResource(R.string.attachment_config_dialog_cancel)
                )
            }
        }
    )
}

@Composable
private fun SharedItemsToggleRow(
    text: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultIcon(
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            icon = CoreR.drawable.ic_proton_users,
            iconColor = PassTheme.colors.interactionNormMajor1,
            size = 28,
            iconSize = 14
        )

        Text.Body1Regular(
            modifier = Modifier.weight(1f),
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PassTheme.colors.interactionNormMajor1
            )
        )
    }
}

@Composable
private fun VaultToggleRow(vault: VaultConfigItem, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultIcon(
            backgroundColor = vault.color.toColor(isBackground = true),
            icon = vault.icon.toResource(),
            iconColor = vault.color.toColor(),
            size = 28,
            iconSize = 14
        )

        Text.Body1Regular(
            modifier = Modifier.weight(1f),
            text = vault.totalSizeBytes
                ?.let { "${vault.name} (${formatFileSize(it)})" }
                ?: vault.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Switch(
            checked = vault.offlineEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PassTheme.colors.interactionNormMajor1
            )
        )
    }
}

@Composable
private fun ToggleRow(
    text: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text.Body1Regular(
            modifier = Modifier.weight(1f),
            text = text
        )

        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PassTheme.colors.interactionNormMajor1
            )
        )
    }
}

@Suppress("MagicNumber")
private fun formatFileSize(sizeInBytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        sizeInBytes >= gb -> String.format(Locale.US, "%.1f GB", sizeInBytes / gb)
        sizeInBytes >= mb -> String.format(Locale.US, "%.1f MB", sizeInBytes / mb)
        sizeInBytes >= kb -> String.format(Locale.US, "%.1f KB", sizeInBytes / kb)
        else -> "$sizeInBytes B"
    }
}

private const val ALERT_DIALOG_HEIGHT_FRACTION = 0.9f
private const val VAULT_LIST_HEIGHT_FRACTION = 0.4f

@Preview
@Composable
internal fun AttachmentConfigDialogPreview(
    @PreviewParameter(AttachmentConfigPreviewProvider::class)
    input: Pair<Boolean, AttachmentConfigState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AttachmentConfigDialogContent(
                state = input.second,
                onAllVaultsToggled = {},
                onVaultToggled = { _, _ -> },
                onSharedItemsToggled = {},
                onCellularToggled = {},
                onStartClick = {},
                onDismiss = {}
            )
        }
    }
}
