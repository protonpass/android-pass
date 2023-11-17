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

package proton.android.pass.domain


@JvmInline
value class ShareRoleId(val id: String)

sealed interface ShareRole {

    val value: String

    object Admin : ShareRole {
        override val value: String = SHARE_ROLE_ADMIN
    }
    object Write : ShareRole {
        override val value: String = SHARE_ROLE_WRITE
    }
    object Read : ShareRole {
        override val value: String = SHARE_ROLE_READ
    }
    data class Custom(val roleId: ShareRoleId) : ShareRole {
        override val value: String = roleId.id
    }

    companion object {
        const val SHARE_ROLE_ADMIN = "1"
        const val SHARE_ROLE_WRITE = "2"
        const val SHARE_ROLE_READ = "3"

        fun fromValue(value: String): ShareRole = when (value) {
            SHARE_ROLE_ADMIN -> Admin
            SHARE_ROLE_WRITE -> Write
            SHARE_ROLE_READ -> Read
            else -> Custom(ShareRoleId(value))
        }
    }
}

fun ShareRole.toPermissions(): SharePermission = when (this) {
    ShareRole.Admin -> SharePermission.fromFlags(
        listOf(
            SharePermissionFlag.Admin,
            SharePermissionFlag.Read,
            SharePermissionFlag.Create,
            SharePermissionFlag.Update,
            SharePermissionFlag.Trash,
            SharePermissionFlag.Delete,
        )
    )
    ShareRole.Write -> SharePermission.fromFlags(
        listOf(
            SharePermissionFlag.Read,
            SharePermissionFlag.Create,
            SharePermissionFlag.Update,
            SharePermissionFlag.Trash,
            SharePermissionFlag.Delete,
        )
    )
    ShareRole.Read -> SharePermission.fromFlags(listOf(SharePermissionFlag.Read))

    // Custom roles not handled yet
    is ShareRole.Custom -> SharePermission.fromFlags(listOf(SharePermissionFlag.Read))
}

