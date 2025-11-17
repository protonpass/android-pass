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
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = UserEventEntity.TABLE,
    primaryKeys = [
        UserEventEntity.Columns.USER_ID
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [UserEventEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserEventEntity(
    @ColumnInfo(name = Columns.ID)
    val eventId: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.RETRIEVED_AT)
    val retrievedAt: Long
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val RETRIEVED_AT = "retrieved_at"
    }
    companion object {
        const val TABLE = "UserEventEntity"
    }
}
