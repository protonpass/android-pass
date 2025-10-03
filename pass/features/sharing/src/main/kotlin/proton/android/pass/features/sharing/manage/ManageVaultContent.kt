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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.SharingNavigation

@Composable
fun ManageVaultContent(
    modifier: Modifier = Modifier,
    state: ManageVaultUiState,
    onNavigateEvent: (SharingNavigation) -> Unit,
    onConfirmInviteClick: (VaultMember.NewUserInvitePending) -> Unit,
    onPendingInvitesClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            BackArrowTopAppBar(
                title = "",
                onUpClick = { onNavigateEvent(SharingNavigation.CloseScreen) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .padding(bottom = Spacing.medium)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            ManageVaultMembersList(
                modifier = Modifier.weight(1f),
                content = state.content,
                vault = state.vault,
                onMemberOptionsClick = { member ->
                    state.vault?.vault?.shareId?.let { shareId ->
                        when (member) {
                            is VaultMember.Member -> {
                                member.role?.let { role ->
                                    val event = SharingNavigation.MemberOptions(
                                        shareId = shareId,
                                        destShareId = member.shareId,
                                        memberRole = role,
                                        destEmail = member.email
                                    )
                                    onNavigateEvent(event)
                                }
                            }

                            is VaultMember.InvitePending -> {
                                val event = SharingNavigation.ExistingUserInviteOptions(
                                    shareId = shareId,
                                    inviteId = member.inviteId
                                )
                                onNavigateEvent(event)
                            }

                            is VaultMember.NewUserInvitePending -> {
                                val event = SharingNavigation.NewUserInviteOptions(
                                    shareId = shareId,
                                    inviteId = member.newUserInviteId
                                )
                                onNavigateEvent(event)
                            }
                        }
                    }
                },
                onConfirmInviteClick = onConfirmInviteClick
            )

            if (state.shareOptions is ShareOptions.Show) {
                ShareWithMorePeopleButton(
                    isEnabled = state.shareOptions.enableButton,
                    onClick = {
                        state.vault?.let { vault ->
                            onNavigateEvent(SharingNavigation.ShareVault(vault.vault.shareId))
                        }
                    }
                )

                when (state.shareOptions.subtitle) {
                    ShareOptions.ShareOptionsSubtitle.LimitReached -> {
                        val limitReachedText = buildAnnotatedString {
                            withStyle(SpanStyle(color = PassTheme.colors.textNorm)) {
                                append(stringResource(id = R.string.sharing_limit_reached))
                            }
                            append(" ")
                            withStyle(SpanStyle(color = PassTheme.colors.interactionNormMajor2)) {
                                append(stringResource(id = R.string.sharing_invites_info_upgrade_text))
                            }
                        }
                        InfoBanner(
                            backgroundColor = PassTheme.colors.interactionNormMinor1,
                            text = limitReachedText,
                            onClick = { onNavigateEvent(SharingNavigation.Upgrade) }
                        )
                    }

                    is ShareOptions.ShareOptionsSubtitle.RemainingInvites -> {
                        RemainingInvitesText(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            remainingInvites = state.shareOptions.subtitle.remainingInvites,
                            onClick = onPendingInvitesClick
                        )
                    }

                    ShareOptions.ShareOptionsSubtitle.None -> {}
                }
            }
        }
    }
}
