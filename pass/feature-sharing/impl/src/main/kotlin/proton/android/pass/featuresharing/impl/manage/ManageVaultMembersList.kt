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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.data.api.usecases.VaultMember
import proton.pass.domain.VaultWithItemCount

@Composable
fun ManageVaultMembersList(
    modifier: Modifier = Modifier,
    content: ManageVaultUiContent,
    vault: VaultWithItemCount?,
    onMemberOptionsClick: (VaultMember) -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ManageVaultHeader(vault = vault)

        Column(modifier = Modifier.roundedContainerNorm()) {
            when (content) {
                ManageVaultUiContent.Loading -> {
                    repeat(2) {
                        ManageVaultMemberRow(
                            member = VaultMemberContent.Loading,
                            canShowActions = false
                        )
                        PassDivider()
                    }
                }

                is ManageVaultUiContent.Content -> {
                    content.vaultMembers.forEach { member ->
                        ManageVaultMemberRow(
                            member = VaultMemberContent.Member(member),
                            canShowActions = content.canEdit,
                            onOptionsClick = { onMemberOptionsClick(member) }
                        )
                        PassDivider()
                    }
                }
            }
        }
    }
}
