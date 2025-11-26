/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.responses.invites

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RetrieveGroupInvitesResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Invites")
    val groupInvitesApiModel: GroupInvitesApiModel
)

@Serializable
data class GroupInvitesApiModel(
    @SerialName("Invites")
    val invites: List<GroupInviteApiModel>,
    @SerialName("Total")
    val total: Int,
    @SerialName("LastID")
    val lastId: String?
)

@Serializable
data class GroupInviteApiModel(
    @SerialName("InviteID")
    val inviteId: String,
    @SerialName("InviterUserID")
    val inviterUserId: String,
    @SerialName("InviterEmail")
    val inviterEmail: String,
    @SerialName("InvitedGroupID")
    val invitedGroupId: String,
    @SerialName("InvitedEmail")
    val invitedEmail: String,
    @SerialName("TargetType")
    val targetType: Int,
    @SerialName("TargetID")
    val targetId: String,
    @SerialName("RemindersSent")
    val remindersSent: Int,
    @SerialName("InviteToken")
    val inviteToken: String,
    @SerialName("InvitedAddressID")
    val invitedAddressId: String,
    @SerialName("Keys")
    val keys: List<KeyApiModel>,
    @SerialName("VaultData")
    val vaultData: VaultDataApiModel?,
    @SerialName("CreateTime")
    val createTime: Long
)
