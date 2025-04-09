/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.data.impl.db.entities.attachments

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.user.data.entity.UserEntity
import proton.android.pass.data.impl.db.entities.ExternalColumns
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.db.entities.ShareEntity

@Entity(
    tableName = AttachmentEntity.TABLE,
    primaryKeys = [AttachmentEntity.Columns.ID, AttachmentEntity.Columns.SHARE_ID, AttachmentEntity.Columns.ITEM_ID],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [AttachmentEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [AttachmentEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = [ItemEntity.Columns.ID, ItemEntity.Columns.SHARE_ID],
            childColumns = [AttachmentEntity.Columns.ITEM_ID, AttachmentEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [AttachmentEntity.Columns.ITEM_ID, AttachmentEntity.Columns.SHARE_ID])
    ]
)
data class AttachmentEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.PERSISTENT_ID, defaultValue = "")
    val persistentId: String,
    @ColumnInfo(name = Columns.USER_ID, index = true)
    val userId: String,
    @ColumnInfo(name = Columns.SHARE_ID, index = true)
    val shareId: String,
    @ColumnInfo(name = Columns.ITEM_ID, index = true)
    val itemId: String,
    @ColumnInfo(name = Columns.METADATA)
    val metadata: String,
    @ColumnInfo(name = Columns.SIZE)
    val size: Long,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Instant,
    @ColumnInfo(name = Columns.MODIFY_TIME)
    val modifyTime: Instant?,
    @ColumnInfo(name = Columns.KEY)
    val key: String,
    @ColumnInfo(name = Columns.ITEM_KEY_ROTATION)
    val itemKeyRotation: String,
    @ColumnInfo(name = Columns.REVISION_ADDED, defaultValue = "1")
    val revisionAdded: Int,
    @ColumnInfo(name = Columns.REVISION_REMOVED)
    val revisionRemoved: Int?,
    @ColumnInfo(name = Columns.REENCRYPTED_KEY)
    val reencryptedKey: EncryptedByteArray,
    @ColumnInfo(name = Columns.REENCRYPTED_METADATA)
    val reencryptedMetadata: EncryptedByteArray,
    @ColumnInfo(name = Columns.ENCRYPTION_VERSION, defaultValue = "1")
    val encryptionVersion: Int
) {
    object Columns {
        const val ID = "id"
        const val PERSISTENT_ID = "persistent_id"
        const val USER_ID = "user_id"
        const val SHARE_ID = "share_id"
        const val ITEM_ID = "item_id"
        const val METADATA = "metadata"
        const val SIZE = "size"
        const val CREATE_TIME = "create_time"
        const val MODIFY_TIME = "modify_time"
        const val KEY = "key"
        const val ITEM_KEY_ROTATION = "item_key_rotation"
        const val REVISION_ADDED = "revision_added"
        const val REVISION_REMOVED = "revision_removed"
        const val REENCRYPTED_KEY = "reencrypted_key"
        const val REENCRYPTED_METADATA = "reencrypted_metadata"
        const val ENCRYPTION_VERSION = "encryptionVersion"
    }

    companion object {
        const val TABLE = "AttachmentEntity"
    }
}
