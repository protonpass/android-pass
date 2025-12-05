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
    tableName = BreachCustomEmailEntity.TABLE,
    primaryKeys = [BreachCustomEmailEntity.Columns.USER_ID, BreachCustomEmailEntity.Columns.CUSTOM_EMAIL_ID],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [BreachCustomEmailEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [BreachCustomEmailEntity.Columns.USER_ID])
    ]
)
data class BreachCustomEmailEntity(
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.CUSTOM_EMAIL_ID)
    val customEmailId: String,
    @ColumnInfo(name = Columns.EMAIL)
    val email: String,
    @ColumnInfo(name = Columns.VERIFIED)
    val verified: Boolean,
    @ColumnInfo(name = Columns.BREACH_COUNT)
    val breachCount: Int,
    @ColumnInfo(name = Columns.FLAGS)
    val flags: Int,
    @ColumnInfo(name = Columns.LAST_BREACH_TIME)
    val lastBreachTime: Long?
) {
    object Columns {
        const val USER_ID = "user_id"
        const val CUSTOM_EMAIL_ID = "custom_email_id"
        const val EMAIL = "email"
        const val VERIFIED = "verified"
        const val BREACH_COUNT = "breach_count"
        const val FLAGS = "flags"
        const val LAST_BREACH_TIME = "last_breach_time"
    }

    companion object {
        const val TABLE = "BreachCustomEmailEntity"
    }
}

