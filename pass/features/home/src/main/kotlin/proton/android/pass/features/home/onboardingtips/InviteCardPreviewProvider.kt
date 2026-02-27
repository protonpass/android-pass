/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.home.onboardingtips

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon

internal data class InviteCardPreviewInput(
    val pendingInvite: PendingInvite,
    val groupName: String?
)

internal class InviteCardPreviewProvider : PreviewParameterProvider<InviteCardPreviewInput> {
    override val values: Sequence<InviteCardPreviewInput> = sequenceOf(
        InviteCardPreviewInput(
            pendingInvite = PendingInvite.UserVault(
                inviteToken = InviteToken(""),
                inviterEmail = "inviter@email.com",
                invitedAddressId = "invitedAddressId",
                isFromNewUser = false,
                memberCount = 3,
                itemCount = 10,
                name = "Vault name",
                icon = ShareIcon.Icon1,
                color = ShareColor.Color1
            ),
            groupName = null
        ),
        InviteCardPreviewInput(
            pendingInvite = PendingInvite.UserItem(
                inviteToken = InviteToken(""),
                inviterEmail = "inviter@email.com",
                invitedAddressId = "invitedAddressId",
                isFromNewUser = false
            ),
            groupName = null
        ),
        InviteCardPreviewInput(
            pendingInvite = PendingInvite.GroupVault(
                inviteToken = InviteToken(""),
                inviterEmail = "inviter@email.com",
                invitedAddressId = "invitedAddressId",
                inviteId = InviteId(""),
                inviterUserId = "inviterUserId",
                invitedGroupId = "groupId",
                invitedEmail = "group@email.com",
                targetId = "targetId",
                remindersSent = 0,
                memberCount = 3,
                itemCount = 10,
                name = "Vault name",
                icon = ShareIcon.Icon1,
                color = ShareColor.Color1
            ),
            groupName = "Engineering"
        ),
        InviteCardPreviewInput(
            pendingInvite = PendingInvite.GroupItem(
                inviteToken = InviteToken(""),
                inviterEmail = "inviter@email.com",
                invitedAddressId = "invitedAddressId",
                inviteId = InviteId(""),
                inviterUserId = "inviterUserId",
                invitedGroupId = "groupId",
                invitedEmail = "group@email.com",
                targetId = "targetId",
                remindersSent = 0
            ),
            groupName = "Engineering"
        )
    )
}

internal class ThemedInviteCardPreviewProvider :
    ThemePairPreviewProvider<InviteCardPreviewInput>(InviteCardPreviewProvider())
