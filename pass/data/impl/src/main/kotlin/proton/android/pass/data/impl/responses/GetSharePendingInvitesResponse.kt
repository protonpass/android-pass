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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetSharePendingInvitesResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Invites")
    val invites: List<SharePendingInvite>,
    @SerialName("NewUserInvites")
    val newUserInvites: List<ShareNewUserPendingInvite>
)

@Serializable
data class SharePendingInvite(
    @SerialName("InviteID")
    val inviteId: String,
    @SerialName("InvitedEmail")
    val invitedEmail: String,
    @SerialName("InviterEmail")
    val inviterEmail: String,
    @SerialName("TargetType")
    val targetType: Int,
    @SerialName("TargetID")
    val targetId: String,
    @SerialName("RemindersSent")
    val remindersSent: Int,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("ModifyTime")
    val modifyTime: Long
)

@Serializable
data class ShareNewUserPendingInvite(
    @SerialName("NewUserInviteID")
    val newUserInviteId: String,
    @SerialName("State")
    val state: Int,
    @SerialName("TargetType")
    val targetType: Int,
    @SerialName("TargetID")
    val targetId: String,
    @SerialName("ShareRoleID")
    val shareRoleId: String,
    @SerialName("InvitedEmail")
    val invitedEmail: String,
    @SerialName("InviterEmail")
    val inviterEmail: String,
    @SerialName("Signature")
    val signature: String,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("ModifyTime")
    val modifyTime: Long
)
