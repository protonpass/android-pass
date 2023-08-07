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

package proton.android.pass.featuresharing.impl.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.item.placeholder
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.featuresharing.impl.common.toShortSummary
import proton.pass.domain.InviteId
import proton.pass.domain.ShareId
import proton.pass.domain.ShareRole
import proton.android.pass.composecomponents.impl.R as CompR

@Stable
sealed interface VaultMemberContent {
    object Loading : VaultMemberContent

    @JvmInline
    value class Member(val member: VaultMember) : VaultMemberContent
}

@Composable
fun ManageVaultMemberRow(
    modifier: Modifier = Modifier,
    member: VaultMemberContent,
    canShowActions: Boolean,
    onOptionsClick: (() -> Unit)? = null
) {
    val (circleTextModifier, circleText) = when (member) {
        VaultMemberContent.Loading -> Modifier.placeholder() to ""
        is VaultMemberContent.Member -> Modifier to member.member.email
    }

    val showActions = when (member) {
        is VaultMemberContent.Member -> when (member.member) {
            is VaultMember.Member -> canShowActions && !member.member.isCurrentUser
            is VaultMember.InvitePending -> true
        }
        VaultMemberContent.Loading -> false
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircleTextIcon(
            modifier = circleTextModifier,
            text = circleText,
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            textColor = PassTheme.colors.interactionNormMajor1,
            shape = PassTheme.shapes.squircleMediumShape
        )

        UserInfo(
            modifier = Modifier.weight(1f),
            member = member
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
}

@Composable
private fun UserInfo(
    modifier: Modifier = Modifier,
    member: VaultMemberContent
) {
    val (titleTextModifier, titleText) = when (member) {
        VaultMemberContent.Loading -> Modifier.fillMaxWidth().placeholder() to ""
        is VaultMemberContent.Member -> Modifier to member.member.email
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            modifier = titleTextModifier,
            text = titleText,
            style = PassTheme.typography.body3Norm()
        )

        when (member) {
            VaultMemberContent.Loading -> {
                Text(
                    modifier = Modifier.fillMaxWidth().placeholder(),
                    text = ""
                )
            }

            is VaultMemberContent.Member -> when (val memberContent = member.member) {
                is VaultMember.Member -> {
                    memberContent.role?.let { role ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (memberContent.isCurrentUser) {
                                Text(
                                    modifier = Modifier.clip(RoundedCornerShape(24.dp))
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
                            Text(
                                text = role.toShortSummary(),
                                style = PassTheme.typography.body3Weak()
                            )
                        }
                    }

                }

                is VaultMember.InvitePending -> {
                    Text(
                        text = stringResource(R.string.share_manage_vault_invite_pending),
                        style = PassTheme.typography.body3Weak()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ManageVaultMemberRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val member = if (input.second) {
        VaultMember.Member(
            email = "some@email.test",
            shareId = ShareId("123"),
            username = "some username",
            role = ShareRole.Admin,
            isCurrentUser = true
        )
    } else {
        VaultMember.InvitePending(email = "invited@email.test", inviteId = InviteId("123"))
    }
    PassTheme(isDark = input.first) {
        Surface {
            ManageVaultMemberRow(
                member = VaultMemberContent.Member(member),
                canShowActions = true,
                onOptionsClick = {}
            )
        }
    }
}
