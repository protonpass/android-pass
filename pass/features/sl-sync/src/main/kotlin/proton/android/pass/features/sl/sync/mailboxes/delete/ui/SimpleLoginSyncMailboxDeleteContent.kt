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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.container.PassInfoWarningBanner
import proton.android.pass.composecomponents.impl.text.PassTextWithInnerStyle
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.mailboxes.delete.presentation.SimpleLoginSyncMailboxDeleteState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncMailboxDeleteContent(
    modifier: Modifier = Modifier,
    state: SimpleLoginSyncMailboxDeleteState,
    onUiEvent: (SimpleLoginSyncMailboxDeleteUiEvent) -> Unit
) = with(state) {
    val deleteTextResId = remember(isTransferAliasesEnabled) {
        if (isTransferAliasesEnabled) {
            R.string.simple_login_sync_mailbox_delete_action_delete_and_transfer
        } else {
            R.string.simple_login_sync_mailbox_delete_action_delete
        }
    }

    Column(
        modifier = modifier
            .bottomSheet()
            .padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.simple_login_sync_mailbox_delete_title),
            textAlign = TextAlign.Center,
            style = PassTheme.typography.body3Bold(),
            color = PassTheme.colors.textNorm
        )

        PassTextWithInnerStyle(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(
                id = R.string.simple_login_sync_mailbox_delete_subtitle,
                aliasMailboxEmail
            ),
            textStyle = PassTheme.typography.body3Norm().copy(color = PassTheme.colors.textNorm),
            innerText = aliasMailboxEmail,
            innerStyle = PassTheme.typography.body3Bold(),
            textAlign = TextAlign.Center
        )

        PassInfoWarningBanner(
            text = stringResource(id = R.string.simple_login_sync_mailbox_delete_warning)
        )

        SimpleLoginSyncMailboxTransferSection(
            isTransferAliasesEnabled = isTransferAliasesEnabled,
            transferAliasMailbox = transferAliasMailboxEmail,
            hasAliasTransferMailboxes = hasAliasTransferMailboxes,
            onUiEvent = onUiEvent
        )

        Spacer(modifier = Modifier.height(height = Spacing.large))

        PassCircleButton(
            text = stringResource(id = deleteTextResId),
            textColor = PassTheme.colors.interactionNormMinor1,
            backgroundColor = PassTheme.colors.signalDanger,
            isLoading = isLoading,
            onClick = {
                onUiEvent(SimpleLoginSyncMailboxDeleteUiEvent.OnDeleteClicked)
            }
        )

        PassCircleButton(
            text = stringResource(id = CompR.string.action_cancel),
            textColor = PassTheme.colors.interactionNormMajor2,
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            onClick = {
                onUiEvent(SimpleLoginSyncMailboxDeleteUiEvent.OnCancelClicked)
            }
        )
    }
}
