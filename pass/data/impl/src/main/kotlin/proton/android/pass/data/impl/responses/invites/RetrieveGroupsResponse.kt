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
data class RetrieveGroupsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Total")
    val total: Int,
    @SerialName("Groups")
    val groups: List<GroupApiModel>
)

@Serializable
data class GroupApiModel(
    @SerialName("ID")
    val id: String,
    @SerialName("Name")
    val name: String,
    @SerialName("Address")
    val address: AddressApiModel?,
    @SerialName("Permissions")
    val permissions: Int,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("Flags")
    val flags: Int,
    @SerialName("GroupVisibility")
    val groupVisibility: Int,
    @SerialName("MemberVisibility")
    val memberVisibility: Int,
    @SerialName("Description")
    val description: String?
)

@Serializable
data class AddressApiModel(
    @SerialName("ID")
    val id: String,
    @SerialName("Email")
    val email: String,
    @SerialName("Keys")
    val keys: List<AddressKeyApiModel>? = null
)

@Serializable
data class AddressKeyApiModel(
    @SerialName("ID")
    val id: String,
    @SerialName("PrivateKey")
    val privateKey: String,
    @SerialName("Token")
    val token: String? = null,
    @SerialName("Signature")
    val signature: String? = null,
    @SerialName("Primary")
    val primary: Int,
    @SerialName("Active")
    val active: Int,
    @SerialName("Flags")
    val flags: Int
)
