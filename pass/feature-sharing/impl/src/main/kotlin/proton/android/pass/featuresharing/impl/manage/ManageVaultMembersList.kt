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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.data.api.usecases.VaultMember

@Composable
fun ColumnScope.ManageVaultMembersList(
    modifier: Modifier = Modifier,
    content: ManageVaultUiContent,
    onMemberOptionsClick: (VaultMember) -> Unit
) {
    when (content) {
        ManageVaultUiContent.Loading -> {
            CircularProgressIndicator(
                modifier = modifier.align(Alignment.CenterHorizontally)
            )
        }

        is ManageVaultUiContent.Content -> {
            Box(modifier = modifier) {
                LazyColumn(modifier = Modifier.roundedContainerNorm()) {
                    items(items = content.vaultMembers, key = { it.email }) { member ->
                        ManageVaultMemberRow(
                            member = member,
                            onOptionsClick = { onMemberOptionsClick(member) }
                        )
                        PassDivider()
                    }
                }
            }
        }
    }
}
