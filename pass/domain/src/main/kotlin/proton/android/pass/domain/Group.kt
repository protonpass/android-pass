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

package proton.android.pass.domain

import me.proton.core.key.domain.entity.key.PrivateAddressKey

data class Group(
    val id: GroupId,
    val name: String,
    val address: GroupAddress?,
    val permissions: Int,
    val createTime: Long,
    val flags: Int,
    val groupVisibility: Int,
    val memberVisibility: Int,
    val description: String?
) {
    val groupEmail = address?.email
}

data class GroupAddress(
    val id: String,
    val email: String,
    val keys: List<PrivateAddressKey>? = null
)

data class GroupMember(
    val id: String,
    val type: Int,
    val state: Int,
    val createTime: Long,
    val groupId: String,
    val addressId: String,
    val email: String
)

