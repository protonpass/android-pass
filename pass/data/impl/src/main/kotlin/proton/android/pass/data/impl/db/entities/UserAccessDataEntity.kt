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
    tableName = UserAccessDataEntity.TABLE,
    primaryKeys = [UserAccessDataEntity.Columns.USER_ID],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [UserAccessDataEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserAccessDataEntity(
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.PENDING_INVITES)
    val pendingInvites: Int,
    @ColumnInfo(name = Columns.WAITING_NEW_USER_INVITES)
    val waitingNewUserInvites: Int,
    @ColumnInfo(name = Columns.MIN_VERSION_UPGRADE)
    val minVersionUpgrade: String?,
    @ColumnInfo(name = Columns.MONITOR_PROTON_ADDRESSES, defaultValue = "1")
    val protonMonitorEnabled: Boolean,
    @ColumnInfo(name = Columns.MONITOR_ALIAS_ADDRESSES, defaultValue = "1")
    val aliasMonitorEnabled: Boolean
) {
    object Columns {
        const val USER_ID = "user_id"
        const val PENDING_INVITES = "pending_invites"
        const val WAITING_NEW_USER_INVITES = "waiting_new_user_invites"
        const val MIN_VERSION_UPGRADE = "min_version_upgrade"
        const val MONITOR_PROTON_ADDRESSES = "monitor_proton_addresses"
        const val MONITOR_ALIAS_ADDRESSES = "monitor_alias_addresses"
    }

    companion object {
        const val TABLE = "UserAccessDataEntity"
    }
}
