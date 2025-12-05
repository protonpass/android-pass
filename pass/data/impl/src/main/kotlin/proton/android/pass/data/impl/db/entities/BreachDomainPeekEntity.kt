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
    tableName = BreachDomainPeekEntity.TABLE,
    primaryKeys = [BreachDomainPeekEntity.Columns.USER_ID, BreachDomainPeekEntity.Columns.BREACH_DOMAIN],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [BreachDomainPeekEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [BreachDomainPeekEntity.Columns.USER_ID])
    ]
)
data class BreachDomainPeekEntity(
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.BREACH_DOMAIN)
    val breachDomain: String,
    @ColumnInfo(name = Columns.BREACH_TIME)
    val breachTime: Long
) {
    object Columns {
        const val USER_ID = "user_id"
        const val BREACH_DOMAIN = "breach_domain"
        const val BREACH_TIME = "breach_time"
    }

    companion object {
        const val TABLE = "BreachDomainPeekEntity"
    }
}

