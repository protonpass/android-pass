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

package proton.android.pass.data.impl.db.entities.attachments

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = ChunkEntity.TABLE,
    primaryKeys = [
        ChunkEntity.Columns.ID,
        ChunkEntity.Columns.ATTACHMENT_ID,
        ChunkEntity.Columns.ITEM_ID,
        ChunkEntity.Columns.SHARE_ID
    ],
    foreignKeys = [
        ForeignKey(
            entity = AttachmentEntity::class,
            parentColumns = [
                AttachmentEntity.Columns.ID,
                AttachmentEntity.Columns.ITEM_ID,
                AttachmentEntity.Columns.SHARE_ID
            ],
            childColumns = [
                ChunkEntity.Columns.ATTACHMENT_ID,
                ChunkEntity.Columns.ITEM_ID,
                ChunkEntity.Columns.SHARE_ID
            ],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [ChunkEntity.Columns.ITEM_ID, ChunkEntity.Columns.SHARE_ID]
        ),
        Index(
            value = [ChunkEntity.Columns.ATTACHMENT_ID, ChunkEntity.Columns.ITEM_ID, ChunkEntity.Columns.SHARE_ID]
        )
    ]
)
data class ChunkEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.ATTACHMENT_ID, index = true)
    val attachmentId: String,
    @ColumnInfo(name = Columns.ITEM_ID, index = true)
    val itemId: String,
    @ColumnInfo(name = Columns.SHARE_ID, index = true)
    val shareId: String,
    @ColumnInfo(name = Columns.SIZE)
    val size: Long,
    @ColumnInfo(name = Columns.INDEX)
    val index: Int
) {
    object Columns {
        const val ID = "id"
        const val ATTACHMENT_ID = "attachment_id"
        const val ITEM_ID = "item_id"
        const val SHARE_ID = "share_id"
        const val SIZE = "size"
        const val INDEX = "chunk_index"
    }

    companion object {
        const val TABLE = "ChunkEntity"
    }
}
