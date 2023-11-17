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

package proton.android.pass.test.domain

import proton.android.pass.common.api.None
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.VaultId
import java.util.Date

object TestShare {
    fun create(
        shareId: ShareId = ShareId("123"),
        shareRole: ShareRole = ShareRole.Admin,
        isOwner: Boolean = true
    ): Share = Share(
        id = shareId,
        shareType = ShareType.Vault,
        targetId = "456",
        permission = SharePermission(SharePermissionFlag.Admin.value),
        vaultId = VaultId("456"),
        content = None,
        expirationTime = null,
        createTime = Date(),
        color = ShareColor.Color1,
        icon = ShareIcon.Icon1,
        shareRole = shareRole,
        isOwner = isOwner
    )
}
