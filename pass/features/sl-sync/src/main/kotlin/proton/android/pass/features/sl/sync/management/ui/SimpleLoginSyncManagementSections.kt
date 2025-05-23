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

package proton.android.pass.features.sl.sync.management.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.widgets.PassSingleActionWidget
import proton.android.pass.domain.Vault
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncLabelText
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncSectionRow
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncManagementSections(
    modifier: Modifier = Modifier,
    defaultDomain: String?,
    aliasMailboxes: ImmutableList<SimpleLoginAliasMailbox>,
    defaultVault: Vault?,
    isSyncEnabled: Boolean,
    hasPendingAliases: Boolean,
    pendingAliasesCount: Int,
    canSelectDomain: Boolean,
    canManageAliases: Boolean,
    onUiEvent: (SimpleLoginSyncManagementUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .padding(all = Spacing.medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.large)
    ) {
        SimpleLoginSyncSectionRow(
            label = stringResource(id = R.string.simple_login_sync_management_domain_label),
            title = stringResource(id = R.string.simple_login_sync_management_domain_title),
            subtitle = defaultDomain ?: stringResource(
                id = R.string.simple_login_sync_management_domain_option_blank
            ),
            onClick = { onUiEvent(SimpleLoginSyncManagementUiEvent.OnDomainClicked) },
            isClickable = canSelectDomain
        )

        SimpleLoginSyncManagementMailboxSection(
            aliasMailboxes = aliasMailboxes,
            canManageAliases = canManageAliases,
            onAddClick = {
                if (canManageAliases) {
                    SimpleLoginSyncManagementUiEvent.OnAddMailboxClicked
                } else {
                    SimpleLoginSyncManagementUiEvent.OnUpsell
                }.also(onUiEvent)
            },
            onMenuClick = { aliasMailbox ->
                SimpleLoginSyncManagementUiEvent.OnMailboxMenuClicked(
                    aliasMailbox = aliasMailbox
                ).also(onUiEvent)
            }
        )

        if (isSyncEnabled && defaultVault != null) {
            with(defaultVault) {
                SimpleLoginSyncSectionRow(
                    leadingIcon = {
                        VaultIcon(
                            backgroundColor = color.toColor(isBackground = true),
                            icon = icon.toResource(),
                            iconColor = color.toColor()
                        )
                    },
                    label = stringResource(id = R.string.simple_login_sync_management_vault_title),
                    title = stringResource(id = R.string.simple_login_sync_shared_default_vault_title),
                    subtitle = name,
                    description = stringResource(
                        id = R.string.simple_login_sync_shared_default_vault_description
                    ),
                    onClick = {
                        SimpleLoginSyncManagementUiEvent.OnDefaultVaultClicked(
                            shareId = shareId
                        ).also(onUiEvent)
                    }
                )
            }
        } else if (hasPendingAliases) {
            Column(
                verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                SimpleLoginSyncLabelText(
                    text = stringResource(id = R.string.simple_login_sync_management_vault_title)
                )

                PassSingleActionWidget(
                    title = stringResource(id = CompR.string.simple_login_widget_title),
                    message = pluralStringResource(
                        id = CompR.plurals.simple_login_widget_pending_aliases_message,
                        count = pendingAliasesCount,
                        pendingAliasesCount
                    ),
                    actionText = stringResource(id = CompR.string.simple_login_widget_action),
                    onActionClick = {
                        SimpleLoginSyncManagementUiEvent.OnSyncSettingsClicked(
                            shareId = defaultVault?.shareId
                        ).also(onUiEvent)
                    }
                )
            }
        }
    }
}
