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

package proton.android.pass.featuresync.impl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.featuresync.impl.R
import proton.android.pass.featuresync.impl.presentation.SyncDialogState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SyncDialogContent(
    modifier: Modifier = Modifier,
    state: SyncDialogState,
    onUiEvent: (SyncDialogUiEvent) -> Unit
) = with(state) {
    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        title = {
            val titlesResId = if (hasSyncFailed) {
                R.string.sync_dialog_title_error
            } else {
                R.string.sync_dialog_title
            }

            Text(
                text = stringResource(id = titlesResId),
                style = ProtonTheme.typography.headlineNorm
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                val subtitleResId = if (hasSyncFailed) {
                    R.string.sync_dialog_subtitle_error
                } else {
                    R.string.sync_dialog_subtitle
                }

                Text(
                    text = stringResource(id = subtitleResId),
                    style = ProtonTheme.typography.defaultNorm
                )

                LazyColumn {
                    items(
                        items = syncItemsMap.entries.toList(),
                        key = { dialogSyncItemMap -> dialogSyncItemMap.key.id }
                    ) { dialogSyncItemMap ->
                        with(dialogSyncItemMap.value) {
                            SyncDialogVaultRow(
                                modifier = Modifier.padding(vertical = Spacing.small),
                                name = vaultName,
                                itemCurrent = currentItemsCount,
                                itemTotal = totalItemsCount,
                                color = vaultColor,
                                icon = vaultIcon,
                                hasSyncFailed = hasSyncFailed
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when {
                hasSyncFailed -> {
                    SyncDialogButton(
                        textResId = CompR.string.action_retry,
                        onClick = { onUiEvent(SyncDialogUiEvent.OnRetrySync) }
                    )
                }

                hasSyncSucceeded -> {
                    SyncDialogButton(
                        textResId = CompR.string.action_continue,
                        onClick = { onUiEvent(SyncDialogUiEvent.OnCompleteSync) }
                    )
                }
            }
        },
        dismissButton = {
            if (hasSyncFailed) {
                SyncDialogButton(
                    textResId = CompR.string.action_not_now,
                    onClick = { onUiEvent(SyncDialogUiEvent.OnCloseSync) }
                )
            }
        }
    )
}
