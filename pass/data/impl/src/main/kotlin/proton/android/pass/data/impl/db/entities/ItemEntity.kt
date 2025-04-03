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
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = ItemEntity.TABLE,
    primaryKeys = [ItemEntity.Columns.ID, ItemEntity.Columns.SHARE_ID],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = [ExternalColumns.ADDRESS_ID],
            childColumns = [ItemEntity.Columns.ADDRESS_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [ItemEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [ItemEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.USER_ID, index = true)
    val userId: String,
    @ColumnInfo(name = Columns.ADDRESS_ID, index = true)
    val addressId: String,
    @ColumnInfo(name = Columns.SHARE_ID, index = true)
    val shareId: String,
    @ColumnInfo(name = Columns.REVISION)
    val revision: Long,
    @ColumnInfo(name = Columns.CONTENT_FORMAT_VERSION)
    val contentFormatVersion: Int,
    @ColumnInfo(name = Columns.KEY_ROTATION)
    val keyRotation: Long,
    @ColumnInfo(name = Columns.CONTENT)
    val content: String,
    @ColumnInfo(name = Columns.KEY)
    val key: String?,
    @ColumnInfo(name = Columns.STATE)
    val state: Int,
    @ColumnInfo(name = Columns.ITEM_TYPE)
    val itemType: Int,
    @ColumnInfo(name = Columns.ALIAS_EMAIL)
    val aliasEmail: String?,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long,
    @ColumnInfo(name = Columns.MODIFY_TIME)
    val modifyTime: Long,
    @ColumnInfo(name = Columns.LAST_USED_TIME)
    val lastUsedTime: Long?,
    @ColumnInfo(name = Columns.HAS_TOTP)
    val hasTotp: Boolean?,
    @ColumnInfo(name = Columns.IS_PINNED, defaultValue = "0")
    val isPinned: Boolean,
    @ColumnInfo(name = Columns.PIN_TIME)
    val pinTime: Long?,
    @ColumnInfo(name = Columns.HAS_PASSKEYS)
    val hasPasskeys: Boolean?,
    @ColumnInfo(name = Columns.FLAGS)
    val flags: Int,
    @ColumnInfo(name = Columns.SHARE_COUNT, defaultValue = "0")
    val shareCount: Int,

    // Keystore Encrypted contents
    @ColumnInfo(name = Columns.ENCRYPTED_TITLE)
    val encryptedTitle: EncryptedString,
    @ColumnInfo(name = Columns.ENCRYPTED_NOTE)
    val encryptedNote: EncryptedString,
    @ColumnInfo(name = Columns.ENCRYPTED_CONTENT)
    val encryptedContent: EncryptedByteArray,
    @ColumnInfo(name = Columns.ENCRYPTED_KEY)
    val encryptedKey: EncryptedByteArray?
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val ADDRESS_ID = "address_id"
        const val SHARE_ID = "share_id"
        const val REVISION = "revision"
        const val CONTENT_FORMAT_VERSION = "content_format_version"
        const val KEY_ROTATION = "key_rotation"
        const val CONTENT = "content"
        const val KEY = "key"
        const val STATE = "state"
        const val ITEM_TYPE = "item_type"
        const val ALIAS_EMAIL = "alias_email"
        const val CREATE_TIME = "create_time"
        const val MODIFY_TIME = "modify_time"
        const val LAST_USED_TIME = "last_used_time"
        const val ENCRYPTED_TITLE = "encrypted_title"
        const val ENCRYPTED_CONTENT = "encrypted_content"
        const val ENCRYPTED_NOTE = "encrypted_note"
        const val ENCRYPTED_KEY = "encrypted_key"
        const val HAS_TOTP = "has_totp"
        const val IS_PINNED = "is_pinned"
        const val PIN_TIME = "pin_time"
        const val HAS_PASSKEYS = "has_passkeys"
        const val FLAGS = "flags"
        const val SHARE_COUNT = "share_count"
    }

    companion object {
        const val TABLE = "ItemEntity"
    }
}
