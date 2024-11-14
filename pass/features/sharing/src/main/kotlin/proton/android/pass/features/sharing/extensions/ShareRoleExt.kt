/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.sharing.extensions

import proton.android.pass.domain.ShareRole
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.sharingpermissions.SharingType
import proton.android.pass.log.api.PassLogger

private const val TAG = "ShareRoleExt"

fun ShareRole.toSharingType() = when (this) {
    is ShareRole.Read -> SharingType.Read
    is ShareRole.Write -> SharingType.Write
    is ShareRole.Admin -> SharingType.Admin

    // As custom roles are not supported yet, apply the least privilege to avoid excessive
    // access
    is ShareRole.Custom -> {
        PassLogger.w(TAG, "ShareRole.Custom not supported yet")
        SharingType.Read
    }
}

internal fun SharingType.toShareRole(): ShareRole = when (this) {
    SharingType.Read -> ShareRole.Read
    SharingType.Write -> ShareRole.Write
    SharingType.Admin -> ShareRole.Admin
}

internal fun SharingType.toStringResource() = when (this) {
    SharingType.Read -> R.string.sharing_can_view
    SharingType.Write -> R.string.sharing_can_edit
    SharingType.Admin -> R.string.sharing_can_manage
}
