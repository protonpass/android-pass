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

package proton.android.pass.features.sharing.accept

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.domain.ShareType
import proton.android.pass.features.sharing.R

@Composable
internal fun AcceptInviteContent(
    modifier: Modifier = Modifier,
    state: AcceptInviteState,
    onUiEvent: (AcceptInviteUiEvent) -> Unit
) = with(state) {
    when (this) {
        AcceptInviteState.Initial -> Unit

        is AcceptInviteState.ItemInvite -> {
            val subtitle = when (val itemInvite = invite) {
                is AcceptInviteUiModel.Item.User -> stringResource(
                    R.string.sharing_item_invitation_subtitle,
                    itemInvite.inviterEmail
                )

                is AcceptInviteUiModel.Item.Group -> stringResource(
                    R.string.sharing_group_item_invitation_subtitle,
                    itemInvite.inviterEmail,
                    itemInvite.groupName
                )
            }
            val acceptTextRes = if (invite is AcceptInviteUiModel.Item.Group) {
                R.string.sharing_group_invitation_accept
            } else {
                R.string.sharing_item_invitation_accept
            }
            AcceptInviteContentBody(
                modifier = modifier,
                title = stringResource(
                    id = R.string.sharing_item_invitation_title
                ),
                subtitle = subtitle,
                inviterEmail = invite.inviterEmail,
                shareType = ShareType.Item,
                acceptInviteText = stringResource(id = acceptTextRes),
                progress = progress,
                onUiEvent = onUiEvent
            )
        }

        is AcceptInviteState.VaultInvite -> {
            val subtitle = when (val vaultInvite = invite) {
                is AcceptInviteUiModel.Vault.User -> stringResource(
                    R.string.sharing_vault_invitation_subtitle,
                    vaultInvite.inviterEmail
                )

                is AcceptInviteUiModel.Vault.Group -> stringResource(
                    R.string.sharing_group_vault_invitation_subtitle,
                    vaultInvite.inviterEmail,
                    vaultInvite.groupName
                )
            }
            val acceptTextRes = if (invite is AcceptInviteUiModel.Vault.Group) {
                R.string.sharing_group_invitation_accept
            } else {
                R.string.sharing_vault_invitation_accept
            }
            AcceptInviteContentBody(
                modifier = modifier,
                title = stringResource(
                    id = R.string.sharing_vault_invitation_title
                ),
                subtitle = subtitle,
                inviterEmail = invite.inviterEmail,
                shareType = ShareType.Vault,
                acceptInviteText = stringResource(id = acceptTextRes),
                onUiEvent = onUiEvent,
                progress = progress,
                infoContent = {
                    AcceptInviteVaultInfo(
                        vaultName = invite.name,
                        vaultItemCount = invite.itemCount,
                        vaultMemberCount = invite.memberCount,
                        vaultIcon = invite.icon,
                        vaultColor = invite.color
                    )
                }
            )
        }
    }
}
