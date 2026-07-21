/*
 * Copyright (c) 2026 Proton AG
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
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = CompromisedPasswordEntity.TABLE,
    primaryKeys = [
        CompromisedPasswordEntity.Columns.USER_ID,
        CompromisedPasswordEntity.Columns.SHARE_ID,
        CompromisedPasswordEntity.Columns.ITEM_ID
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [CompromisedPasswordEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [CompromisedPasswordEntity.Columns.USER_ID])
    ]
)
data class CompromisedPasswordEntity(
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String,
    @ColumnInfo(name = Columns.ITEM_ID)
    val itemId: String,
    @ColumnInfo(name = Columns.IS_COMPROMISED)
    val isCompromised: Boolean,
    @ColumnInfo(name = Columns.PASSWORD_HASH, defaultValue = "")
    val passwordHash: String,
    @ColumnInfo(name = Columns.CHECKED_AT)
    val checkedAt: Long,
    @ColumnInfo(name = Columns.LAST_ETAG, defaultValue = "NULL")
    val lastEtag: String?
) {
    object Columns {
        const val USER_ID = "user_id"
        const val SHARE_ID = "share_id"
        const val ITEM_ID = "item_id"
        const val IS_COMPROMISED = "is_compromised"
        const val PASSWORD_HASH = "password_hash"
        const val CHECKED_AT = "checked_at"
        const val LAST_ETAG = "last_etag"
    }

    companion object {
        const val TABLE = "CompromisedPasswordEntity"
    }
}
