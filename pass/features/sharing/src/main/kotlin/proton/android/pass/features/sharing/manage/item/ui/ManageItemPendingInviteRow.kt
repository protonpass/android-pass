/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.features.sharing.manage.item.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.shares.SharePendingInvite
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.common.toShortSummary
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ManageItemPendingInviteRow(
    modifier: Modifier = Modifier,
    pendingInvite: SharePendingInvite,
    displayName: String = pendingInvite.email,
    groupId: GroupId? = null,
    memberCount: Int = 0,
    isRenameAdminToManagerEnabled: Boolean,
    onMenuOptionsClick: (SharePendingInvite) -> Unit,
    onViewGroupMembersClick: (GroupId) -> Unit = {}
) {
    val subtitleText = when (pendingInvite) {
        is SharePendingInvite.ExistingUser -> {
            stringResource(R.string.share_manage_vault_invite_pending)
        }

        is SharePendingInvite.NewUser -> {
            when (pendingInvite.inviteState) {
                SharePendingInvite.NewUser.InviteState.PendingAccountCreation -> {
                    stringResource(R.string.share_manage_vault_invite_pending_user_creation)
                }

                SharePendingInvite.NewUser.InviteState.PendingAcceptance -> {
                    pendingInvite.role.toShortSummary(isRenameAdminToManagerEnabled)
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.mediumSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleTextIcon(
            modifier = Modifier.padding(start = Spacing.medium),
            text = displayName,
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            textColor = PassTheme.colors.interactionNormMajor2,
            shape = PassTheme.shapes.squircleMediumShape
        )

        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .weight(weight = 1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
        ) {
            if (groupId != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
                ) {
                    Text.Body2Regular(
                        modifier = Modifier.weight(1f, fill = false),
                        text = displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val membersLabel = "(${
                        pluralStringResource(CompR.plurals.members_count, memberCount, memberCount)
                    })"
                    val membersModifier = if (memberCount > 0) {
                        Modifier.clickable { onViewGroupMembersClick(groupId) }
                    } else {
                        Modifier
                    }
                    Text.Body2Regular(
                        modifier = membersModifier,
                        text = membersLabel,
                        color = PassTheme.colors.interactionNormMajor2
                    )
                }
            } else {
                Text.Body2Regular(text = displayName)
            }

            Text.Body2Regular(
                text = subtitleText,
                color = PassTheme.colors.textWeak
            )
        }

        ThreeDotsMenuButton(
            onClick = { onMenuOptionsClick(pendingInvite) }
        )
    }
}
