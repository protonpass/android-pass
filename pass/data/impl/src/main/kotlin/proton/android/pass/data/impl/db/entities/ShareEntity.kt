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

package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity
import proton.android.pass.domain.ShareRole

@Entity(
    tableName = ShareEntity.TABLE,
    primaryKeys = [ShareEntity.Columns.ID],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [ShareEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [ShareEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShareEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.USER_ID, index = true)
    val userId: String,
    @ColumnInfo(name = Columns.ADDRESS_ID, index = true)
    val addressId: String,
    @ColumnInfo(name = Columns.VAULT_ID)
    val vaultId: String,
    @ColumnInfo(name = Columns.GROUP_ID)
    val groupId: String?,
    @ColumnInfo(name = Columns.SHARE_TYPE)
    val targetType: Int,
    @ColumnInfo(name = Columns.TARGET_ID)
    val targetId: String,
    @ColumnInfo(name = Columns.PERMISSION)
    val permission: Int,
    @ColumnInfo(name = Columns.CONTENT)
    val content: String?,
    @ColumnInfo(name = Columns.CONTENT_KEY_ROTATION)
    val contentKeyRotation: Long?,
    @ColumnInfo(name = Columns.CONTENT_FORMAT_VERSION)
    val contentFormatVersion: Int?,
    @ColumnInfo(name = Columns.EXPIRATION_TIME)
    val expirationTime: Long?,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long,

    // Keystore Encrypted contents
    @ColumnInfo(name = Columns.ENCRYPTED_CONTENT)
    val encryptedContent: EncryptedByteArray?,

    @ColumnInfo(name = Columns.IS_ACTIVE, defaultValue = "1")
    val isActive: Boolean,
    @ColumnInfo(name = Columns.SHARE_ROLE_ID, defaultValue = ShareRole.SHARE_ROLE_ADMIN)
    val shareRoleId: String,
    @ColumnInfo(name = Columns.OWNER, defaultValue = "1")
    val owner: Boolean,
    @ColumnInfo(name = Columns.TARGET_MEMBERS, defaultValue = "1")
    val targetMembers: Int,
    @ColumnInfo(name = Columns.SHARED, defaultValue = "0")
    val shared: Boolean,
    @ColumnInfo(name = Columns.TARGET_MAX_MEMBERS, defaultValue = "10")
    val targetMaxMembers: Int,
    @ColumnInfo(name = Columns.PENDING_INVITES, defaultValue = "0")
    val pendingInvites: Int,
    @ColumnInfo(name = Columns.NEW_USER_INVITES_READY, defaultValue = "0")
    val newUserInvitesReady: Int,
    @ColumnInfo(name = Columns.CAN_AUTOFILL, defaultValue = "1")
    val canAutofill: Boolean,
    @ColumnInfo(name = Columns.FLAGS, defaultValue = "0")
    val flags: Int,
    @ColumnInfo(name = Columns.GROUP_EMAIL)
    val groupEmail: String?
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val SHARE_TYPE = "share_type"
        const val TARGET_ID = "target_id"
        const val PERMISSION = "permission"
        const val IS_PRIMARY = "is_primary"
        const val VAULT_ID = "vault_id"
        const val GROUP_ID = "group_id"
        const val GROUP_EMAIL = "group_email"
        const val CONTENT = "content"
        const val CONTENT_KEY_ROTATION = "content_key_rotation"
        const val CONTENT_FORMAT_VERSION = "content_format_version"
        const val EXPIRATION_TIME = "expiration_time"
        const val CREATE_TIME = "create_time"
        const val ENCRYPTED_CONTENT = "encrypted_content"
        const val IS_ACTIVE = "is_active"
        const val SHARE_ROLE_ID = "share_role_id"
        const val OWNER = "owner"
        const val TARGET_MEMBERS = "target_members"
        const val SHARED = "shared"
        const val TARGET_MAX_MEMBERS = "target_max_members"
        const val PENDING_INVITES = "pending_invites"
        const val NEW_USER_INVITES_READY = "new_user_invites_ready"
        const val CAN_AUTOFILL = "can_autofill"
        const val FLAGS = "flags"
    }

    companion object {
        const val TABLE = "ShareEntity"
    }
}
