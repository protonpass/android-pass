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

package proton.android.pass.features.sl.sync.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.widgets.PassSingleActionWidget
import proton.android.pass.domain.Vault
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncLabelText
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncSectionRow
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncDetailsSections(
    modifier: Modifier = Modifier,
    defaultDomain: String,
    defaultMailboxEmail: String,
    defaultVaultOption: Option<Vault>,
    pendingAliasesCount: Int,
    canSelectDomain: Boolean,
    canSelectMailbox: Boolean,
    onUiEvent: (SimpleLoginSyncDetailsUiEvent) -> Unit
) {
    Column(
        modifier = modifier.padding(all = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        SimpleLoginSyncSectionRow(
            label = stringResource(id = R.string.simple_login_sync_details_domain_label),
            title = stringResource(id = R.string.simple_login_sync_details_domain_title),
            subtitle = defaultDomain.ifEmpty { "Not Selected" },
            onClick = { onUiEvent(SimpleLoginSyncDetailsUiEvent.OnDomainClicked) },
            isClickable = canSelectDomain
        )

        SimpleLoginSyncSectionRow(
            label = stringResource(id = R.string.simple_login_sync_details_mailboxes_label),
            title = stringResource(id = R.string.simple_login_sync_details_mailboxes_title),
            subtitle = defaultMailboxEmail,
            onClick = { onUiEvent(SimpleLoginSyncDetailsUiEvent.OnMailboxClicked) },
            isClickable = canSelectMailbox
        )

        when (defaultVaultOption) {
            None -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
                ) {
                    SimpleLoginSyncLabelText(
                        text = stringResource(id = R.string.simple_login_sync_details_vault_title)
                    )

                    PassSingleActionWidget(
                        title = stringResource(id = CompR.string.simple_login_widget_title),
                        message = stringResource(
                            id = CompR.string.simple_login_widget_message,
                            pluralStringResource(
                                id = CompR.plurals.simple_login_widget_pending_aliases,
                                count = pendingAliasesCount,
                                pendingAliasesCount
                            )
                        ),
                        actionText = stringResource(id = CompR.string.simple_login_widget_action),
                        onActionClick = { onUiEvent(SimpleLoginSyncDetailsUiEvent.OnSyncSettingsClicked) }
                    )
                }
            }

            is Some -> {
                with(defaultVaultOption.value) {
                    SimpleLoginSyncSectionRow(
                        leadingIcon = {
                            VaultIcon(
                                backgroundColor = color.toColor(isBackground = true),
                                icon = icon.toResource(),
                                iconColor = color.toColor()
                            )
                        },
                        label = stringResource(id = R.string.simple_login_sync_details_vault_title),
                        title = stringResource(id = R.string.simple_login_sync_shared_default_vault_title),
                        subtitle = name,
                        description = stringResource(
                            id = R.string.simple_login_sync_shared_default_vault_description
                        ),
                        onClick = {
                            SimpleLoginSyncDetailsUiEvent.OnDefaultVaultClicked(
                                shareId = shareId
                            ).also(onUiEvent)
                        }
                    )
                }
            }
        }
    }
}
