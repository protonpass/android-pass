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

package proton.android.pass.features.sharing.manage.item.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.common.toShortSummary

@Composable
internal fun ManageItemMemberRow(
    modifier: Modifier = Modifier,
    member: ShareMember,
    canAdmin: Boolean,
    hasVaultAccess: Boolean,
    isRenameAdminToManagerEnabled: Boolean,
    onMenuOptionsClick: (ShareMember) -> Unit
) {
    val showMemberShareRole = remember(member.isItemMember, hasVaultAccess) {
        member.isItemMember || hasVaultAccess
    }

    val showMemberOptions = remember(canAdmin, member.isOwner, member.isCurrentUser) {
        canAdmin && !member.isOwner && !member.isCurrentUser
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleTextIcon(
            modifier = Modifier.padding(start = Spacing.medium),
            text = member.email,
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
            Text.Body2Regular(
                text = member.email
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                if (member.isCurrentUser) {
                    Text.OverlineRegular(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = Radius.medium))
                            .background(color = PassTheme.colors.interactionNormMajor2)
                            .padding(
                                horizontal = Spacing.small,
                                vertical = Spacing.extraSmall
                            ),
                        text = stringResource(id = R.string.share_manage_vault_current_user_indicator),
                        color = PassTheme.colors.textInvert
                    )
                }

                if (showMemberShareRole) {
                    Text.Body2Regular(
                        text = if (member.isOwner) {
                            stringResource(id = R.string.share_role_owner)
                        } else {
                            member.role.toShortSummary(isRenameAdminToManagerEnabled)
                        },
                        color = PassTheme.colors.textWeak
                    )
                }
            }
        }

        if (showMemberOptions) {
            ThreeDotsMenuButton(
                onClick = { onMenuOptionsClick(member) }
            )
        }
    }
}
