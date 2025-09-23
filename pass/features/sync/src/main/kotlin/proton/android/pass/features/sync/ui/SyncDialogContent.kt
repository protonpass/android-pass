/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.sync.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.sync.R
import proton.android.pass.features.sync.presentation.SyncDialogState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SyncDialogContent(
    modifier: Modifier = Modifier,
    state: SyncDialogState,
    onUiEvent: (SyncDialogUiEvent) -> Unit
) = with(state) {
    ProtonAlertDialog(
        modifier = modifier
            .wrapContentHeight()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * ALERT_DIALOG_HEIGHT_FRACTION),
        onDismissRequest = {},
        title = {
            val titlesResId = when {
                hasSyncFailed -> R.string.sync_dialog_title_error
                hasSyncSucceeded -> R.string.sync_dialog_title_success
                isInserting -> R.string.sync_dialog_title_inserting
                else -> R.string.sync_dialog_title
            }

            Text(
                text = stringResource(id = titlesResId),
                style = ProtonTheme.typography.headlineNorm
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val subtitleResId = when {
                    hasSyncFailed -> R.string.sync_dialog_subtitle_error
                    hasSyncSucceeded -> R.string.sync_dialog_subtitle_success
                    isInserting -> R.string.sync_dialog_subtitle_inserting
                    else -> R.string.sync_dialog_subtitle
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = subtitleResId),
                    style = ProtonTheme.typography.defaultNorm
                )

                when {
                    isInserting -> {
                        Spacer(modifier = Modifier.height(Spacing.small))
                        CircularProgressIndicator()
                    }

                    else -> LazyColumn {
                        items(
                            items = syncItemsMap.entries.toList(),
                            key = { dialogSyncItemMap -> dialogSyncItemMap.key.id }
                        ) { dialogSyncItemMap ->
                            with(dialogSyncItemMap.value) {
                                SyncDialogVaultRow(
                                    modifier = Modifier.padding(vertical = Spacing.small),
                                    name = vaultName,
                                    itemCurrent = currentDownloadedItemsCount,
                                    itemTotal = totalDownloadedItemsCount,
                                    color = vaultColor,
                                    icon = vaultIcon,
                                    hasSyncFailed = hasSyncFailed,
                                    hasSyncFinished = hasSyncFinished
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            AnimatedVisibility(visible = hasSyncFailed) {
                SyncDialogButton(
                    textResId = CompR.string.action_retry,
                    onClick = { onUiEvent(SyncDialogUiEvent.OnRetrySync) }
                )
            }

            AnimatedVisibility(visible = hasSyncSucceeded) {
                SyncDialogButton(
                    textResId = CompR.string.action_continue,
                    onClick = { onUiEvent(SyncDialogUiEvent.OnCompleteSync) }
                )
            }
        },
        dismissButton = {
            AnimatedVisibility(visible = hasSyncFailed) {
                SyncDialogButton(
                    textResId = CompR.string.action_not_now,
                    onClick = { onUiEvent(SyncDialogUiEvent.OnCloseSync) }
                )
            }
        }
    )
}

private const val ALERT_DIALOG_HEIGHT_FRACTION = 0.9f
