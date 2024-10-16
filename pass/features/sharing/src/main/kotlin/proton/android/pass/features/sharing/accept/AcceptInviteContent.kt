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

package proton.android.pass.features.sharing.accept

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.features.sharing.R

@Composable
fun AcceptInviteContent(
    modifier: Modifier = Modifier,
    state: AcceptInviteUiContent.Content,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    val invite = state.invite ?: return
    Column(
        modifier = modifier.padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.sharing_invitation_from, invite.inviterEmail),
            style = PassTheme.typography.body3Norm(),
            color = PassTheme.colors.textNorm
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
            "$itemCount ${SpecialCharacters.DOT_SEPARATOR} $memberCount"
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
            onConfirm = onConfirm,
            onReject = onReject
        )

        AnimatedVisibility(visible = state.progressState is AcceptInviteProgressState.Show) {
            if (state.progressState is AcceptInviteProgressState.Show) {
                AcceptInviteItemSyncStatus(
                    downloaded = state.progressState.downloaded,
                    total = state.progressState.total
                )
            }
        }
    }
}

@Preview
@Composable
fun AcceptInviteContentPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            AcceptInviteContent(
                state = AcceptInviteUiContent.Content(
                    invite = PendingInvite(
                        inviteToken = InviteToken(""),
                        inviterEmail = "some@inviter.test",
                        invitedAddressId = "invitedAddressId",
                        memberCount = 2,
                        itemCount = 3,
                        name = "Some vault",
                        icon = ShareIcon.Icon1,
                        color = ShareColor.Color1,
                        fromNewUser = false
                    ),
                    buttonsState = AcceptInviteButtonsState(
                        confirmLoading = input.second,
                        rejectLoading = false,
                        enabled = !input.second,
                        hideReject = false
                    ),
                    progressState = AcceptInviteProgressState.Hide
                ),
                onConfirm = {},
                onReject = {}
            )
        }
    }
}
