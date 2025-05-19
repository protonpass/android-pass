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

package proton.android.pass.features.sharing.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.modifiers.placeholder
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.common.toShortSummary
import proton.android.pass.composecomponents.impl.R as CompR

@Stable
sealed interface VaultMemberContent {
    data object Loading : VaultMemberContent

    data class Member(
        val member: VaultMember,
        val isLoading: Boolean = false
    ) : VaultMemberContent
}

@Composable
fun ManageVaultMemberRow(
    modifier: Modifier = Modifier,
    member: VaultMemberContent,
    canShowActions: Boolean,
    isRenameAdminToManagerEnabled: Boolean,
    onOptionsClick: (() -> Unit)? = null,
    onConfirmInviteClick: ((VaultMember.NewUserInvitePending) -> Unit)? = null
) {
    val (circleTextModifier, circleText) = when (member) {
        VaultMemberContent.Loading -> Modifier.placeholder() to ""
        is VaultMemberContent.Member -> Modifier to member.member.email
    }

    val showActions = when (member) {
        is VaultMemberContent.Member -> when (member.member) {
            is VaultMember.Member -> {
                canShowActions && !member.member.isCurrentUser && !member.member.isOwner
            }

            is VaultMember.InvitePending -> canShowActions
            is VaultMember.NewUserInvitePending -> canShowActions
        }

        VaultMemberContent.Loading -> false
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.mediumSmall),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            CircleTextIcon(
                modifier = circleTextModifier,
                text = circleText,
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                textColor = PassTheme.colors.interactionNormMajor2,
                shape = PassTheme.shapes.squircleMediumShape
            )

            UserInfo(
                modifier = Modifier.weight(1f),
                member = member,
                isRenameAdminToManagerEnabled = isRenameAdminToManagerEnabled
            )

            if (showActions) {
                IconButton(
                    onClick = { onOptionsClick?.invoke() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(CompR.drawable.ic_three_dots_vertical_24),
                        contentDescription = stringResource(id = CompR.string.action_content_description_menu),
                        tint = PassTheme.colors.textHint
                    )
                }
            }
        }

        ConfirmAccessButton(
            member = member,
            onClick = { onConfirmInviteClick?.invoke(it) }
        )
    }
}

@Composable
private fun UserInfo(
    modifier: Modifier = Modifier,
    member: VaultMemberContent,
    isRenameAdminToManagerEnabled: Boolean
) {
    val (titleTextModifier, titleText) = when (member) {
        VaultMemberContent.Loading ->
            Modifier
                .fillMaxWidth()
                .placeholder() to ""

        is VaultMemberContent.Member -> Modifier to member.member.email
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        Text(
            modifier = titleTextModifier,
            text = titleText,
            style = PassTheme.typography.body3Norm()
        )

        UserInfoSubtitle(member = member, isRenameAdminToManagerEnabled = isRenameAdminToManagerEnabled)
    }
}

@Composable
private fun UserInfoSubtitle(
    modifier: Modifier = Modifier,
    member: VaultMemberContent,
    isRenameAdminToManagerEnabled: Boolean
) {
    when (member) {
        VaultMemberContent.Loading -> {
            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .placeholder(),
                text = ""
            )
        }

        is VaultMemberContent.Member -> when (val memberContent = member.member) {
            is VaultMember.Member -> {
                memberContent.role?.let { role ->
                    Row(
                        modifier = modifier,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (memberContent.isCurrentUser) {
                            Text(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        color = PassTheme.colors.interactionNorm,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                text = stringResource(R.string.share_manage_vault_current_user_indicator),
                                color = PassTheme.colors.textNorm,
                                style = ProtonTheme.typography.captionNorm
                            )
                        }

                        val subtitle = if (memberContent.isOwner) {
                            stringResource(R.string.share_role_owner)
                        } else {
                            role.toShortSummary(isRenameAdminToManagerEnabled)
                        }
                        Text(
                            text = subtitle,
                            style = PassTheme.typography.body3Weak()
                        )
                    }
                }
            }

            is VaultMember.InvitePending -> {
                Text(
                    modifier = modifier,
                    text = stringResource(R.string.share_manage_vault_invite_pending),
                    style = PassTheme.typography.body3Weak()
                )
            }

            is VaultMember.NewUserInvitePending -> {
                val text = when (memberContent.inviteState) {
                    VaultMember.NewUserInvitePending.InviteState.PendingAccountCreation -> {
                        stringResource(R.string.share_manage_vault_invite_pending_user_creation)
                    }

                    VaultMember.NewUserInvitePending.InviteState.PendingAcceptance -> {
                        memberContent.role.toShortSummary(isRenameAdminToManagerEnabled)
                    }
                }
                Text(
                    modifier = modifier,
                    text = text,
                    style = PassTheme.typography.body3Weak()
                )
            }
        }
    }
}

@Composable
private fun ConfirmAccessButton(
    modifier: Modifier = Modifier,
    member: VaultMemberContent,
    onClick: (VaultMember.NewUserInvitePending) -> Unit
) {
    if (member is VaultMemberContent.Member &&
        member.member is VaultMember.NewUserInvitePending &&
        member.member.inviteState == VaultMember.NewUserInvitePending.InviteState.PendingAcceptance
    ) {
        CircleButton(
            modifier = modifier.fillMaxWidth(),
            color = PassTheme.colors.interactionNormMinor1,
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = { onClick(member.member) }
        ) {
            if (member.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(
                    modifier = Modifier.padding(vertical = 10.dp),
                    text = stringResource(R.string.share_manage_vault_invite_confirm_access),
                    color = PassTheme.colors.interactionNormMajor2
                )
            }
        }
    }
}

@Preview
@Composable
fun ManageVaultMemberRowPreview(
    @PreviewParameter(ThemedMVMPreviewProvider::class) input: Pair<Boolean, VaultMemberRowInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ManageVaultMemberRow(
                member = input.second.member,
                canShowActions = true,
                isRenameAdminToManagerEnabled = true,
                onOptionsClick = {}
            )
        }
    }
}
