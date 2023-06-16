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

package proton.pass.domain

enum class SharePermissionFlag(val value: Int) {
    Admin(1 shl 0),
    Read(1 shl 1),
    Create(1 shl 2),
    Update(1 shl 3),
    Trash(1 shl 4),
    Delete(1 shl 5),
    CreateLabel(1 shl 6),
    TrashLabel(1 shl 7),
    AttachLabel(1 shl 8),
    DetachLabel(1 shl 9);

    companion object {
        val map = values().associateBy { it.value }
    }
}
