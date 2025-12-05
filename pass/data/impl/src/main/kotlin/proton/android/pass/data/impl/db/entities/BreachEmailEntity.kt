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

package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = BreachEmailEntity.TABLE,
    primaryKeys = [
        BreachEmailEntity.Columns.BREACH_ID,
        BreachEmailEntity.Columns.EMAIL_TYPE,
        BreachEmailEntity.Columns.EMAIL_OWNER_ID,
        BreachEmailEntity.Columns.USER_ID
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [BreachEmailEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                BreachEmailEntity.Columns.USER_ID,
                BreachEmailEntity.Columns.EMAIL_TYPE,
                BreachEmailEntity.Columns.EMAIL_OWNER_ID
            ]
        ),
        Index(value = [BreachEmailEntity.Columns.USER_ID])
    ]
)
data class BreachEmailEntity(
    @ColumnInfo(name = Columns.BREACH_ID)
    val breachId: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.EMAIL_TYPE)
    val emailType: Int, // 0=Custom, 1=Proton, 2=Alias
    @ColumnInfo(name = Columns.EMAIL_OWNER_ID)
    val emailOwnerId: String, // customEmailId for Custom, addressId for Proton, composite for Alias
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String?, // For Alias type
    @ColumnInfo(name = Columns.ITEM_ID)
    val itemId: String?, // For Alias type
    @ColumnInfo(name = Columns.EMAIL)
    val email: String,
    @ColumnInfo(name = Columns.SEVERITY)
    val severity: Double,
    @ColumnInfo(name = Columns.NAME)
    val name: String,
    @ColumnInfo(name = Columns.CREATED_AT)
    val createdAt: String,
    @ColumnInfo(name = Columns.PUBLISHED_AT)
    val publishedAt: String,
    @ColumnInfo(name = Columns.SIZE)
    val size: Long?,
    @ColumnInfo(name = Columns.PASSWORD_LAST_CHARS)
    val passwordLastChars: String?,
    @ColumnInfo(name = Columns.EXPOSED_DATA)
    val exposedData: String, // JSON serialized List<String>
    @ColumnInfo(name = Columns.IS_RESOLVED)
    val isResolved: Boolean,
    @ColumnInfo(name = Columns.ACTIONS)
    val actions: String // JSON serialized List<BreachAction>
) {
    object Columns {
        const val BREACH_ID = "breach_id"
        const val USER_ID = "user_id"
        const val EMAIL_TYPE = "email_type"
        const val EMAIL_OWNER_ID = "email_owner_id"
        const val SHARE_ID = "share_id"
        const val ITEM_ID = "item_id"
        const val EMAIL = "email"
        const val SEVERITY = "severity"
        const val NAME = "name"
        const val CREATED_AT = "created_at"
        const val PUBLISHED_AT = "published_at"
        const val SIZE = "size"
        const val PASSWORD_LAST_CHARS = "password_last_chars"
        const val EXPOSED_DATA = "exposed_data"
        const val IS_RESOLVED = "is_resolved"
        const val ACTIONS = "actions"
    }

    companion object {
        const val TABLE = "BreachEmailEntity"
        const val EMAIL_TYPE_CUSTOM = 0
        const val EMAIL_TYPE_PROTON = 1
        const val EMAIL_TYPE_ALIAS = 2
    }
}

