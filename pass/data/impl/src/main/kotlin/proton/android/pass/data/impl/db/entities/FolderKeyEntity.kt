/*
 * Copyright (c) 2025 Proton AG
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
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = FolderKeyEntity.TABLE,
    primaryKeys = [FolderKeyEntity.Columns.FOLDER_ID, FolderKeyEntity.Columns.SHARE_ID],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [FolderKeyEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [FolderKeyEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = [FolderEntity.Columns.ID, FolderEntity.Columns.SHARE_ID],
            childColumns = [FolderKeyEntity.Columns.FOLDER_ID, FolderKeyEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FolderKeyEntity(
    @ColumnInfo(name = Columns.FOLDER_ID, index = true)
    val folderId: String,
    @ColumnInfo(name = Columns.USER_ID, index = true)
    val userId: String,
    @ColumnInfo(name = Columns.SHARE_ID, index = true)
    val shareId: String,
    @ColumnInfo(name = Columns.KEY_ROTATION)
    val keyRotation: Long,
    @ColumnInfo(name = Columns.ENCRYPTED_KEY)
    val encryptedKey: EncryptedByteArray
) {
    object Columns {
        const val FOLDER_ID = "folder_id"
        const val USER_ID = "user_id"
        const val SHARE_ID = "share_id"
        const val KEY_ROTATION = "key_rotation"
        const val ENCRYPTED_KEY = "encrypted_key"
    }

    companion object {
        const val TABLE = "FolderKeyEntity"
    }
}
