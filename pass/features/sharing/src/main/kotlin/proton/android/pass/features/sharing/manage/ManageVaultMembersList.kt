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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.sharing.R

@Composable
fun ManageVaultMembersList(
    modifier: Modifier = Modifier,
    content: ManageVaultUiContent,
    vault: VaultWithItemCount?,
    onMemberOptionsClick: (VaultMember) -> Unit,
    onConfirmInviteClick: (VaultMember.NewUserInvitePending) -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ManageVaultHeader(vault = vault)

        when (content) {
            ManageVaultUiContent.Loading -> {
                Column(modifier = Modifier.roundedContainerNorm()) {
                    repeat(2) {
                        ManageVaultMemberRow(
                            member = VaultMemberContent.Loading,
                            isRenameAdminToManagerEnabled = false,
                            canShowActions = false
                        )
                        PassDivider()
                    }
                }
            }

            is ManageVaultUiContent.Content -> {
                ManageVaultMembersList(
                    content = content,
                    onMemberOptionsClick = onMemberOptionsClick,
                    onConfirmInviteClick = onConfirmInviteClick
                )
            }
        }
    }
}

@Composable
private fun ManageVaultMembersList(
    modifier: Modifier = Modifier,
    content: ManageVaultUiContent.Content,
    onMemberOptionsClick: (VaultMember) -> Unit,
    onConfirmInviteClick: (VaultMember.NewUserInvitePending) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (content.invites.isNotEmpty()) {
            Text(
                text = stringResource(R.string.share_manage_vault_invitations_title),
                color = PassTheme.colors.textWeak
            )

            Column(modifier = Modifier.roundedContainerNorm()) {
                content.invites.forEach { invite ->

                    val isLoading = if (invite is VaultMember.NewUserInvitePending) {
                        content.loadingInvites.contains(invite.newUserInviteId)
                    } else {
                        false
                    }

                    ManageVaultMemberRow(
                        member = VaultMemberContent.Member(
                            member = invite,
                            isLoading = isLoading
                        ),
                        canShowActions = content.canEdit,
                        onOptionsClick = { onMemberOptionsClick(invite) },
                        isRenameAdminToManagerEnabled = content.isRenameAdminToManagerEnabled,
                        onConfirmInviteClick = onConfirmInviteClick
                    )
                    PassDivider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = stringResource(R.string.share_manage_vault_members_title),
            color = PassTheme.colors.textWeak
        )
        Column(modifier = Modifier.roundedContainerNorm()) {
            content.vaultMembers.forEach { member ->
                ManageVaultMemberRow(
                    member = VaultMemberContent.Member(
                        member = member
                    ),
                    canShowActions = content.canEdit,
                    isRenameAdminToManagerEnabled = content.isRenameAdminToManagerEnabled,
                    onOptionsClick = { onMemberOptionsClick(member) }
                )
                PassDivider()
            }
        }
    }
}
