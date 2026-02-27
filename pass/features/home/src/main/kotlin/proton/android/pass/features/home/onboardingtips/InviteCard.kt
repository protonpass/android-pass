/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.home.onboardingtips

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.domain.PendingInvite
import proton.android.pass.features.home.R

@Composable
internal fun InviteCard(
    modifier: Modifier = Modifier,
    pendingInvite: PendingInvite,
    groupName: String?,
    onClick: () -> Unit
) {
    val title = when (pendingInvite) {
        is PendingInvite.GroupItem,
        is PendingInvite.UserItem -> stringResource(R.string.home_item_invite_card_title)
        is PendingInvite.GroupVault,
        is PendingInvite.UserVault -> stringResource(R.string.home_vault_invite_card_title)
    }
    val body = when (pendingInvite) {
        is PendingInvite.GroupItem -> stringResource(
            R.string.home_group_item_invite_banner_title,
            pendingInvite.inviterEmail,
            groupName ?: pendingInvite.invitedEmail
        )
        is PendingInvite.GroupVault -> stringResource(
            R.string.home_group_invite_banner_title,
            pendingInvite.inviterEmail,
            groupName ?: pendingInvite.invitedEmail
        )
        is PendingInvite.UserItem -> stringResource(
            R.string.home_item_invite_banner_title,
            pendingInvite.inviterEmail
        )
        is PendingInvite.UserVault -> stringResource(
            R.string.home_vault_invite_banner_body,
            pendingInvite.inviterEmail
        )
    }

    SpotlightCard(
        modifier = modifier,
        backgroundColor = PassTheme.colors.backgroundMedium,
        title = title,
        body = body,
        titleColor = PassTheme.colors.textNorm,
        image = {
            Image(
                modifier = Modifier.size(60.dp),
                alignment = Alignment.CenterEnd,
                painter = painterResource(id = R.drawable.spotlight_invite),
                contentDescription = null
            )
        },
        buttonText = null,
        onClick = onClick,
        onDismiss = null
    )
}

@[Preview Composable]
internal fun InviteCardPreview(
    @PreviewParameter(ThemedInviteCardPreviewProvider::class) input: Pair<Boolean, InviteCardPreviewInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            InviteCard(
                pendingInvite = input.second.pendingInvite,
                groupName = input.second.groupName,
                onClick = {}
            )
        }
    }
}
