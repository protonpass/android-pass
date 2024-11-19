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

package proton.android.pass.features.sharing.confirmed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InviteConfirmedContent(
    modifier: Modifier = Modifier,
    state: InviteConfirmedUiContent.Content,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    val invite = state.invite ?: return
//    Column(
//        modifier = modifier.padding(horizontal = Spacing.medium),
//        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = stringResource(R.string.sharing_invitation_access_confirmed_title),
//            style = ProtonTheme.typography.headlineNorm,
//            color = PassTheme.colors.textNorm,
//            textAlign = TextAlign.Center
//        )
//
//        VaultIcon(
//            backgroundColor = invite.color.toColor(isBackground = true),
//            iconColor = invite.color.toColor(isBackground = false),
//            icon = invite.icon.toResource(),
//            size = 64,
//            iconSize = 32
//        )
//
//        Text(
//            text = invite.name,
//            style = ProtonTheme.typography.headlineNorm,
//            color = PassTheme.colors.textNorm
//        )
//
//        val itemCount = pluralStringResource(
//            R.plurals.sharing_item_count,
//            invite.itemCount,
//            invite.itemCount
//        )
//        val memberCount = pluralStringResource(
//            R.plurals.sharing_member_count,
//            invite.memberCount,
//            invite.memberCount
//        )
//        val subtitle = remember(invite.itemCount, invite.memberCount) {
//            "$itemCount ${SpecialCharacters.DOT_SEPARATOR} $memberCount"
//        }
//        Text(
//            text = subtitle,
//            style = ProtonTheme.typography.defaultNorm,
//            color = PassTheme.colors.textWeak
//        )
//
//        AcceptInviteButtons(
//            isConfirmLoading = state.buttonsState.confirmLoading,
//            isRejectLoading = state.buttonsState.rejectLoading,
//            areButtonsEnabled = state.buttonsState.enabled,
//            showReject = !state.buttonsState.hideReject,
//            confirmText = stringResource(R.string.sharing_invitation_access_confirmed_accept),
//            rejectText = stringResource(R.string.sharing_invitation_access_confirmed_close),
//            onConfirm = onConfirm,
//            onReject = onReject
//        )
//
//        AnimatedVisibility(visible = state.progressState is InviteConfirmedProgressState.Show) {
//            if (state.progressState is InviteConfirmedProgressState.Show) {
//                AcceptInviteItemSyncStatus(
//                    downloaded = state.progressState.downloaded,
//                    total = state.progressState.total
//                )
//            }
//        }
//    }
}
