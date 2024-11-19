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
data class PendingInvitesResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Invites")
    val invites: List<PendingInviteResponse>
)

@Serializable
data class PendingInviteResponse(
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
    val keys: List<PendingInviteKey>,
    @SerialName("VaultData")
    val vaultData: InviteVaultData?,
    @SerialName("FromNewUser")
    val fromNewUser: Boolean,
    @SerialName("CreateTime")
    val createTime: Long
)

@Serializable
data class InviteVaultData(
    @SerialName("Content")
    val content: String,
    @SerialName("ContentKeyRotation")
    val contentKeyRotation: Long,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("MemberCount")
    val memberCount: Int,
    @SerialName("ItemCount")
    val itemCount: Int
)

@Serializable
data class PendingInviteKey(
    @SerialName("Key")
    val key: String,
    @SerialName("KeyRotation")
    val keyRotation: Long
)

