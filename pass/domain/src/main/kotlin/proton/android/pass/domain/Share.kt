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

import me.proton.core.domain.entity.UserId
import java.util.Date

@JvmInline
value class ShareId(val id: String)

@JvmInline
value class VaultId(val id: String)

sealed class Share {

    abstract val id: ShareId

    abstract val userId: UserId

    abstract val shareType: ShareType

    abstract val targetId: String

    abstract val vaultId: VaultId

    abstract val expirationTime: Date?

    abstract val createTime: Date

    abstract val shareRole: ShareRole

    abstract val isOwner: Boolean

    abstract val memberCount: Int

    abstract val shared: Boolean

    abstract val maxMembers: Int

    abstract val pendingInvites: Int

    abstract val newUserInvitesReady: Int

    abstract val canAutofill: Boolean

    protected abstract val permission: SharePermission

    data class Item(
        override val id: ShareId,
        override val userId: UserId,
        override val targetId: String,
        override val permission: SharePermission,
        override val vaultId: VaultId,
        override val expirationTime: Date?,
        override val createTime: Date,
        override val shareRole: ShareRole,
        override val isOwner: Boolean,
        override val memberCount: Int,
        override val shared: Boolean,
        override val maxMembers: Int,
        override val pendingInvites: Int,
        override val newUserInvitesReady: Int,
        override val canAutofill: Boolean
    ) : Share() {

        override val shareType: ShareType = ShareType.Item

    }

    data class Vault(
        override val id: ShareId,
        override val userId: UserId,
        override val targetId: String,
        override val permission: SharePermission,
        override val vaultId: VaultId,
        override val expirationTime: Date?,
        override val createTime: Date,
        override val shareRole: ShareRole,
        override val isOwner: Boolean,
        override val memberCount: Int,
        override val shared: Boolean,
        override val maxMembers: Int,
        override val pendingInvites: Int,
        override val newUserInvitesReady: Int,
        override val canAutofill: Boolean,
        val name: String,
        val color: ShareColor,
        val icon: ShareIcon
    ) : Share() {

        override val shareType: ShareType = ShareType.Vault

    }

    private val totalMembers: Int
        get() = memberCount
            .plus(pendingInvites)
            .plus(newUserInvitesReady)

    val remainingInvites: Int
        get() = maxMembers.minus(totalMembers)

    val hasRemainingInvites: Boolean
        get() = remainingInvites > 0

    val canBeCreated: Boolean
        get() = shareRole.toPermissions().canCreate()

    val canBeDeleted: Boolean
        get() = shareRole.toPermissions().canDelete()

    val canBeTrashed: Boolean
        get() = shareRole.toPermissions().canTrash()

    val canBeUpdated: Boolean
        get() = shareRole.toPermissions().canUpdate()

    val isAdmin: Boolean
        get() = permission.hasFlag(SharePermissionFlag.Admin)

}
