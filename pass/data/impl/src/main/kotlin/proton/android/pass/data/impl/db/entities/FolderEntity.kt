/*
 * Copyright (c) 2025-2026 Proton AG
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
import androidx.room.Index
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = FolderEntity.TABLE,
    primaryKeys = [FolderEntity.Columns.ID, FolderEntity.Columns.SHARE_ID],
    indices = [
        Index(
            value = [
                FolderEntity.Columns.USER_ID,
                FolderEntity.Columns.SHARE_ID,
                FolderEntity.Columns.PARENT_FOLDER_ID
            ]
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [FolderEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [FolderEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FolderEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.USER_ID, index = true)
    val userId: String,
    @ColumnInfo(name = Columns.SHARE_ID, index = true)
    val shareId: String,
    @ColumnInfo(name = Columns.VAULT_ID)
    val vaultId: String,
    @ColumnInfo(name = Columns.PARENT_FOLDER_ID)
    val parentFolderId: String?,
    @ColumnInfo(name = Columns.KEY_ROTATION)
    val keyRotation: Long,
    @ColumnInfo(name = Columns.CONTENT_FORMAT_VERSION)
    val contentFormatVersion: Int,
    @ColumnInfo(name = Columns.CONTENT)
    val content: String,
    @ColumnInfo(name = Columns.FOLDER_KEY)
    val folderKey: String,

    // Keystore Encrypted contents
    @ColumnInfo(name = Columns.ENCRYPTED_CONTENT)
    val encryptedContent: EncryptedByteArray
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val SHARE_ID = "share_id"
        const val VAULT_ID = "vault_id"
        const val PARENT_FOLDER_ID = "parent_folder_id"
        const val KEY_ROTATION = "key_rotation"
        const val CONTENT_FORMAT_VERSION = "content_format_version"
        const val CONTENT = "content"
        const val FOLDER_KEY = "folder_key"
        const val ENCRYPTED_CONTENT = "encrypted_content"
    }

    companion object {
        const val TABLE = "FolderEntity"
    }
}
