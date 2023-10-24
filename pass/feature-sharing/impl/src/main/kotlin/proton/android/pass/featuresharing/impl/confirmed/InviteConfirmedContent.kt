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

package proton.android.pass.featuresharing.impl.confirmed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.accept.AcceptInviteButtons
import proton.android.pass.featuresharing.impl.accept.AcceptInviteItemSyncStatus

@Composable
fun InviteConfirmedContent(
    modifier: Modifier = Modifier,
    state: InviteConfirmedUiContent.Content,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    val invite = state.invite ?: return
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.sharing_invitation_access_confirmed_title),
            style = ProtonTheme.typography.headlineNorm,
            color = PassTheme.colors.textNorm,
            textAlign = TextAlign.Center
        )

        VaultIcon(
            backgroundColor = invite.color.toColor(isBackground = true),
            iconColor = invite.color.toColor(isBackground = false),
            icon = invite.icon.toResource(),
            size = 64,
            iconSize = 32
        )

        Text(
            text = invite.name,
            style = ProtonTheme.typography.headlineNorm,
            color = PassTheme.colors.textNorm
        )

        val itemCount = pluralStringResource(
            R.plurals.sharing_item_count,
            invite.itemCount,
            invite.itemCount
        )
        val memberCount = pluralStringResource(
            R.plurals.sharing_member_count,
            invite.memberCount,
            invite.memberCount
        )
        val subtitle = remember(invite.itemCount, invite.memberCount) {
            "$itemCount • $memberCount"
        }
        Text(
            text = subtitle,
            style = ProtonTheme.typography.defaultNorm,
            color = PassTheme.colors.textWeak
        )

        AcceptInviteButtons(
            isConfirmLoading = state.buttonsState.confirmLoading,
            isRejectLoading = state.buttonsState.rejectLoading,
            areButtonsEnabled = state.buttonsState.enabled,
            showReject = !state.buttonsState.hideReject,
            confirmText = stringResource(R.string.sharing_invitation_access_confirmed_accept),
            rejectText = stringResource(R.string.sharing_invitation_access_confirmed_close),
            onConfirm = onConfirm,
            onReject = onReject
        )

        AnimatedVisibility(visible = state.progressState is InviteConfirmedProgressState.Show) {
            if (state.progressState is InviteConfirmedProgressState.Show) {
                AcceptInviteItemSyncStatus(
                    downloaded = state.progressState.downloaded,
                    total = state.progressState.total
                )
            }
        }
    }
}
