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

package proton.android.pass.features.sl.sync.mailboxes.delete.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.features.sl.sync.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SimpleLoginSyncMailboxTransferSection(
    modifier: Modifier = Modifier,
    isTransferAliasesEnabled: Boolean,
    transferAliasMailbox: String,
    hasAliasTransferMailboxes: Boolean,
    onUiEvent: (SimpleLoginSyncMailboxDeleteUiEvent) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.simple_login_sync_mailbox_delete_tranfer_option_toggle),
                color = PassTheme.colors.textNorm,
                style = ProtonTheme.typography.body2Regular
            )

            Switch(
                checked = isTransferAliasesEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PassTheme.colors.interactionNorm,
                    checkedTrackColor = PassTheme.colors.interactionNormMajor1
                ),
                onCheckedChange = { isChecked ->
                    SimpleLoginSyncMailboxDeleteUiEvent.OnTransferAliasesToggled(
                        isTransferAliasesEnabled = isChecked
                    ).also(onUiEvent)
                }
            )
        }

        AnimatedVisibility(visible = isTransferAliasesEnabled) {
            Column(
                verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
            ) {
                PassDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.simple_login_sync_mailbox_delete_tranfer_option_mailbox),
                        color = PassTheme.colors.textNorm,
                        style = ProtonTheme.typography.body2Regular
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(size = Radius.small))
                            .background(color = PassTheme.colors.interactionNormMinor1)
                            .applyIf(
                                condition = hasAliasTransferMailboxes,
                                ifTrue = { clickable {} }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(all = Spacing.small),
                            text = transferAliasMailbox,
                            color = PassTheme.colors.textNorm,
                            style = ProtonTheme.typography.body2Regular
                        )

                        if (hasAliasTransferMailboxes) {
                            Icon(
                                modifier = Modifier.padding(end = Spacing.extraSmall),
                                painter = painterResource(CoreR.drawable.ic_proton_chevron_tiny_down),
                                contentDescription = null,
                                tint = PassTheme.colors.textWeak
                            )
                        }
                    }
                }
            }
        }
    }
}
