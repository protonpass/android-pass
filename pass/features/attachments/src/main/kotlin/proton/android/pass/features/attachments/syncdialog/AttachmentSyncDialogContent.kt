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

package proton.android.pass.features.attachments.syncdialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonAlertDialog
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.messages.OfflineIndicator
import proton.android.pass.features.attachments.R
import proton.android.pass.features.attachments.syncdialog.AttachmentSyncDialogState.SyncStatus
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun AttachmentSyncDialogContent(
    modifier: Modifier = Modifier,
    state: AttachmentSyncDialogState,
    onUiEvent: (AttachmentSyncDialogUiEvent) -> Unit
) {
    ProtonAlertDialog(
        modifier = modifier
            .wrapContentHeight()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * ALERT_DIALOG_HEIGHT_FRACTION),
        onDismissRequest = {},
        title = {
            val titleResId = when (state.status) {
                SyncStatus.Loading,
                SyncStatus.WaitingForConnection -> R.string.attachment_sync_dialog_title_waiting
                SyncStatus.Downloading -> R.string.attachment_sync_dialog_title_downloading
                SyncStatus.Done -> R.string.attachment_sync_dialog_title_done
                SyncStatus.PartialFailure -> R.string.attachment_sync_dialog_title_partial_failure
            }

            Text.Headline(
                text = stringResource(id = titleResId)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val subtitleResId = when (state.status) {
                    SyncStatus.Loading,
                    SyncStatus.WaitingForConnection -> R.string.attachment_sync_dialog_subtitle_waiting
                    SyncStatus.Downloading -> R.string.attachment_sync_dialog_subtitle_downloading
                    SyncStatus.Done -> R.string.attachment_sync_dialog_subtitle_done
                    SyncStatus.PartialFailure -> R.string.attachment_sync_dialog_subtitle_partial_failure
                }

                Text.Body1Regular(
                    modifier = Modifier.fillMaxWidth(),
                    text = when (state.status) {
                        SyncStatus.Downloading -> stringResource(
                            id = subtitleResId,
                            state.downloaded,
                            state.total
                        )

                        else -> stringResource(id = subtitleResId)
                    }
                )

                when (state.status) {
                    SyncStatus.WaitingForConnection -> {
                        Spacer(modifier = Modifier.height(Spacing.small))
                        OfflineIndicator()
                    }

                    SyncStatus.Loading -> {
                        Spacer(modifier = Modifier.height(Spacing.small))
                        CircularProgressIndicator()
                    }

                    SyncStatus.Downloading,
                    SyncStatus.PartialFailure,
                    SyncStatus.Done -> Unit
                }

                if (state.vaults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    LazyColumn {
                        items(
                            items = state.vaults,
                            key = { vault -> vault.shareId.id }
                        ) { vault ->
                            AttachmentSyncVaultRow(
                                vault = vault,
                                onRetryAttachment = { attachment ->
                                    onUiEvent(
                                        AttachmentSyncDialogUiEvent.OnRetryAttachment(attachment)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            AttachmentSyncDialogButton(
                textResId = CompR.string.action_close,
                onClick = { onUiEvent(AttachmentSyncDialogUiEvent.OnClose) }
            )
        },
        dismissButton = {
            val hasAnyFailed = state.vaults.any { it.hasFailed }
            AnimatedVisibility(visible = state.status == SyncStatus.PartialFailure && hasAnyFailed) {
                AttachmentSyncDialogButton(
                    textResId = CompR.string.action_retry,
                    onClick = { onUiEvent(AttachmentSyncDialogUiEvent.OnRetryAll) }
                )
            }
        }
    )
}

private const val ALERT_DIALOG_HEIGHT_FRACTION = 0.9f

@Preview
@Composable
internal fun AttachmentSyncDialogContentPreview(
    @PreviewParameter(AttachmentSyncDialogContentPreviewProvider::class)
    input: Pair<Boolean, AttachmentSyncDialogState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AttachmentSyncDialogContent(
                state = input.second,
                onUiEvent = {}
            )
        }
    }
}
