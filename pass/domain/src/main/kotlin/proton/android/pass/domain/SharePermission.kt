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

import me.proton.core.util.kotlin.hasFlag

data class SharePermission(val value: Int) {
    companion object {
        fun fromFlags(flags: List<SharePermissionFlag>): SharePermission =
            SharePermission(flags.fold(0) { acc, flag -> acc.or(flag.value) })
    }
}

fun SharePermission.hasFlag(flag: SharePermissionFlag): Boolean = value.hasFlag(flag.value)
fun SharePermission.flags(): List<SharePermissionFlag> = SharePermissionFlag.map.values.filter { hasFlag(it) }.toList()

fun SharePermission.canUpdate(): Boolean = hasFlag(SharePermissionFlag.Admin) or hasFlag(SharePermissionFlag.Update)

fun SharePermission.canClone(): Boolean = hasFlag(SharePermissionFlag.Admin)

fun SharePermission.canCreate(): Boolean = hasFlag(SharePermissionFlag.Admin) or hasFlag(SharePermissionFlag.Create)

fun SharePermission.canDelete(): Boolean = hasFlag(SharePermissionFlag.Admin) or hasFlag(SharePermissionFlag.Delete)

fun SharePermission.canTrash(): Boolean = hasFlag(SharePermissionFlag.Admin) or hasFlag(SharePermissionFlag.Trash)
