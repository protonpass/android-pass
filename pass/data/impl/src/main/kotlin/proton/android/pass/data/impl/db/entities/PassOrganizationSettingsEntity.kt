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
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = PassOrganizationSettingsEntity.TABLE,
    primaryKeys = [
        PassOrganizationSettingsEntity.Columns.USER_ID,
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [PassOrganizationSettingsEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PassOrganizationSettingsEntity(
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.CAN_UPDATE)
    val canUpdate: Boolean,
    @ColumnInfo(name = Columns.SHARE_MODE)
    val shareMode: Int,
    @ColumnInfo(name = Columns.HAS_ORGANIZATION)
    val hasOrganization: Boolean
) {
    object Columns {
        const val USER_ID = "user_id"
        const val CAN_UPDATE = "can_update"
        const val SHARE_MODE = "share_mode"
        const val HAS_ORGANIZATION = "has_organization"
    }
    companion object {
        const val TABLE = "PassOrganizationSettingsEntity"

        fun empty(userId: String) = PassOrganizationSettingsEntity(
            userId = userId,
            canUpdate = false,
            shareMode = 0,
            hasOrganization = false
        )
    }
}
