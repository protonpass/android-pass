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
import proton.android.pass.data.impl.responses.invites.KeyApiModel
import proton.android.pass.data.impl.responses.invites.VaultDataApiModel

@Serializable
data class PendingUserInvitesResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Invites")
    val invites: List<PendingUserInviteResponse>
)

@Serializable
data class PendingUserInviteResponse(
    @SerialName("InviteToken")
    val inviteToken: String,
    @SerialName("RemindersSent")
    val remindersSent: Int,
    @SerialName("TargetType")
    val targetType: Int,
    @SerialName("TargetID")
    val targetId: String,
    @SerialName("InviterEmail")
    val inviterEmail: String,
    @SerialName("InvitedEmail")
    val invitedEmail: String,
    @SerialName("InvitedAddressID")
    val invitedAddressId: String,
    @SerialName("Keys")
    val keys: List<KeyApiModel>,
    @SerialName("VaultData")
    val vaultData: VaultDataApiModel?,
    @SerialName("FromNewUser")
    val fromNewUser: Boolean,
    @SerialName("CreateTime")
    val createTime: Long
)
