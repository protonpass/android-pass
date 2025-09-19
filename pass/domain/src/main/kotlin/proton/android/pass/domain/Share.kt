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
import me.proton.core.util.kotlin.hasFlag
import java.util.Date

@JvmInline
value class ShareId(val id: String)

@JvmInline
value class VaultId(val id: String)

@JvmInline
value class ShareFlags(val value: Int) {
    fun isHidden(): Boolean = value.hasFlag(ShareFlag.IsHidden.value)
}

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

    abstract val canBeDeleted: Boolean

    abstract val canBeHistoryViewed: Boolean

    abstract val canBeSelected: Boolean

    abstract val canBeTrashed: Boolean

    abstract val canBeUpdated: Boolean

    abstract val shareFlags: ShareFlags

    abstract val canBeCloned: Boolean

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
        override val canAutofill: Boolean,
        override val shareFlags: ShareFlags
    ) : Share() {

        override val shareType: ShareType = ShareType.Item

        override val canBeDeleted: Boolean = isOwner || isAdmin || isEditor

        override val canBeHistoryViewed: Boolean = isOwner || isAdmin || isEditor

        override val canBeSelected: Boolean = false

        override val canBeTrashed: Boolean = isOwner || isAdmin || isEditor

        override val canBeUpdated: Boolean = isOwner || isAdmin || isEditor

        override val canBeCloned: Boolean = isOwner || isAdmin

        val isSharingAvailable: Boolean = isOwner || isAdmin || isEditor

        val canBeShared: Boolean = isOwner || isAdmin

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
        override val shareFlags: ShareFlags,
        val name: String,
        val color: ShareColor,
        val icon: ShareIcon
    ) : Share() {

        override val shareType: ShareType = ShareType.Vault

        override val canBeDeleted: Boolean
            get() = shareRole.toPermissions().canDelete()

        override val canBeHistoryViewed: Boolean = false

        override val canBeSelected: Boolean
            get() = shareRole.toPermissions().canCreate()

        override val canBeTrashed: Boolean
            get() = shareRole.toPermissions().canTrash()

        override val canBeUpdated: Boolean
            get() = shareRole.toPermissions().canUpdate()

        override val canBeCloned: Boolean
            get() = shareRole.toPermissions().canClone()

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

    val isAdmin: Boolean
        get() = shareRole == ShareRole.Admin

    val isEditor: Boolean
        get() = shareRole == ShareRole.Write

    val isViewer: Boolean
        get() = shareRole == ShareRole.Read

}
