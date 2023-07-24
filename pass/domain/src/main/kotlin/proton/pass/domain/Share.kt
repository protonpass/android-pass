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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.common.api.Option
import java.util.Date

@JvmInline
value class ShareId(val id: String)

@JvmInline
value class VaultId(val id: String)

data class Share(
    val id: ShareId,
    val shareType: ShareType,
    val targetId: String,
    val permission: SharePermission,
    val isPrimary: Boolean,
    val vaultId: VaultId,
    val content: Option<EncryptedByteArray>, // Can be None if targetType is Item
    val expirationTime: Date?,
    val createTime: Date,
    val color: ShareColor,
    val icon: ShareIcon,
    val shareRole: ShareRole,
    val isOwner: Boolean
)
