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

package proton.android.pass.featuresync.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme

@Composable
fun SyncContent(
    modifier: Modifier = Modifier,
    state: SyncDialogUiState,
    onNavigate: (SyncNavigation) -> Unit
) {
    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(R.string.sync_dialog_title),
                style = ProtonTheme.typography.headlineNorm
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.sync_dialog_subtitle),
                    style = ProtonTheme.typography.defaultNorm
                )

                LazyColumn {
                    items(items = state.syncItemMap.entries.toList(), key = { it.key.id }) { item ->
                        SyncVaultRow(
                            modifier = Modifier.padding(0.dp, 8.dp),
                            name = item.value.vault.name,
                            itemCurrent = item.value.current,
                            itemTotal = item.value.total,
                            color = item.value.vault.color,
                            icon = item.value.vault.icon
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (state.isFinished) {
                ProtonTextButton(
                    onClick = { onNavigate(SyncNavigation.FinishedFetching) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        disabledBackgroundColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = stringResource(R.string.sync_dialog_confirm_button),
                        color = PassTheme.colors.interactionNormMajor1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        dismissButton = {

        }
    )
}
