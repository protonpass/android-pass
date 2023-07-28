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
import androidx.room.Index

@Entity(
    tableName = SearchEntryEntity.TABLE,
    primaryKeys = [SearchEntryEntity.Columns.SHARE_ID, SearchEntryEntity.Columns.ITEM_ID],
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = [ItemEntity.Columns.ID, ItemEntity.Columns.SHARE_ID],
            childColumns = [SearchEntryEntity.Columns.ITEM_ID, SearchEntryEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [SearchEntryEntity.Columns.ITEM_ID, SearchEntryEntity.Columns.SHARE_ID],
            unique = true
        )
    ]
)
data class SearchEntryEntity(
    @ColumnInfo(name = Columns.ITEM_ID, index = true)
    val itemId: String,
    @ColumnInfo(name = Columns.SHARE_ID, index = true)
    val shareId: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long
) {
    object Columns {
        const val ITEM_ID = "item_id"
        const val SHARE_ID = "share_id"
        const val USER_ID = "user_id"
        const val CREATE_TIME = "create_time"
    }

    companion object {
        const val TABLE = "SearchEntryEntity"
    }
}
