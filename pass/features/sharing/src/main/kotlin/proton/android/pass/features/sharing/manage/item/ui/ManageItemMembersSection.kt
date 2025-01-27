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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.Vault
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.domain.shares.SharePendingInvite

@Composable
internal fun ManageItemMembersSection(
    modifier: Modifier = Modifier,
    sectionTitle: String,
    isItemSection: Boolean,
    isShareAdmin: Boolean,
    vaultOption: Option<Vault>,
    shareItemsCount: Int,
    pendingInvites: List<SharePendingInvite>,
    members: List<ShareMember>,
    onPendingInviteMenuOptionsClick: (SharePendingInvite) -> Unit,
    onMemberMenuOptionsClick: (ShareMember) -> Unit,
    onInviteMoreClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Text.Body3Regular(
            modifier = Modifier.padding(bottom = Spacing.small),
            text = sectionTitle,
            color = PassTheme.colors.textWeak
        )

        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            vaultOption.value()?.let { vaultShare ->
                ManageItemVaultRow(
                    vault = vaultShare,
                    vaultItemsCount = shareItemsCount
                )

                PassDivider()

                if (isShareAdmin) {
                    ManageItemInviteMoreRow(
                        onClick = onInviteMoreClick
                    )

                    PassDivider()
                }
            }

            if (isItemSection && isShareAdmin) {
                ManageItemInviteMoreRow(
                    onClick = onInviteMoreClick
                )

                PassDivider()
            }

            pendingInvites.forEach { pendingInvite ->
                ManageItemPendingInviteRow(
                    pendingInvite = pendingInvite,
                    onMenuOptionsClick = onPendingInviteMenuOptionsClick
                )

                PassDivider()
            }

            members.forEachIndexed { index, member ->
                ManageItemMemberRow(
                    member = member,
                    canAdmin = isShareAdmin,
                    hasVaultAccess = vaultOption is Some,
                    onMenuOptionsClick = onMemberMenuOptionsClick
                )

                if (index < members.lastIndex) {
                    PassDivider()
                }
            }
        }
    }
}
