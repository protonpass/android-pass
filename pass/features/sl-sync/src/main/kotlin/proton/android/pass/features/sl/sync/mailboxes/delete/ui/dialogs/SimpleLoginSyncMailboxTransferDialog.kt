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

package proton.android.pass.features.sl.sync.mailboxes.delete.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.features.sl.sync.R

@Composable
internal fun SimpleLoginSyncMailboxTransferDialog(
    modifier: Modifier = Modifier,
    selectedTransferAliasMailboxId: Long?,
    transferAliasMailboxes: ImmutableList<SimpleLoginAliasMailbox>,
    onTransferAliasMailboxSelected: (SimpleLoginAliasMailbox) -> Unit,
    onDismiss: () -> Unit
) {
    NoPaddingDialog(
        modifier = modifier.padding(horizontal = Spacing.medium),
        backgroundColor = PassTheme.colors.backgroundStrong,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.large,
                    end = Spacing.large,
                    bottom = Spacing.large
                ),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
        ) {
            ProtonDialogTitle(
                modifier = Modifier.padding(
                    top = Spacing.large,
                    bottom = Spacing.medium
                ),
                title = stringResource(id = R.string.simple_login_sync_mailbox_delete_transfer_dialog_title)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
            ) {
                items(
                    items = transferAliasMailboxes,
                    key = { transferAliasMailbox -> transferAliasMailbox.id }
                ) { aliasMailbox ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .applyIf(
                                condition = selectedTransferAliasMailboxId != aliasMailbox.id,
                                ifTrue = {
                                    clickable { onTransferAliasMailboxSelected(aliasMailbox) }
                                }
                            ),
                        horizontalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTransferAliasMailboxId == aliasMailbox.id,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PassTheme.colors.interactionNormMajor2
                            )
                        )

                        Text(
                            text = aliasMailbox.email,
                            style = ProtonTheme.typography.body2Regular,
                            color = PassTheme.colors.textNorm
                        )
                    }
                }
            }
        }
    }
}
