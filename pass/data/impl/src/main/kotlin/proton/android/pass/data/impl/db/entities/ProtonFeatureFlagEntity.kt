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
    tableName = ProtonFeatureFlagEntity.TABLE,
    primaryKeys = [
        ProtonFeatureFlagEntity.Columns.USER_ID,
        ProtonFeatureFlagEntity.Columns.NAME
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [ShareEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProtonFeatureFlagEntity(
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.NAME)
    val name: String,
    @ColumnInfo(name = Columns.VARIANT)
    val variant: String,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long,
    @ColumnInfo(name = Columns.UPDATE_TIME)
    val updateTime: Long
) {
    object Columns {
        const val USER_ID = "user_id"
        const val NAME = "name"
        const val VARIANT = "variant"
        const val CREATE_TIME = "create_time"
        const val UPDATE_TIME = "update_time"
    }

    companion object {
        const val TABLE = "ProtonFeatureFlagEntity"
    }
}
