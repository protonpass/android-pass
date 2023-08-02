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
data class GetShareMembersResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Total")
    val total: Int,
    @SerialName("Shares")
    val members: List<ShareMemberResponse>
)

@Serializable
data class ShareMemberResponse(
    @SerialName("ShareID")
    val shareId: String,
    @SerialName("UserName")
    val userName: String,
    @SerialName("UserEmail")
    val userEmail: String,
    @SerialName("TargetType")
    val targetType: Int,
    @SerialName("TargetID")
    val targetId: String,
    @SerialName("Permission")
    val permission: Int,
    @SerialName("ShareRoleID")
    val shareRoleId: String?,
    @SerialName("ExpireTime")
    val expireTime: Long?,
    @SerialName("CreateTime")
    val createTime: Long
)
