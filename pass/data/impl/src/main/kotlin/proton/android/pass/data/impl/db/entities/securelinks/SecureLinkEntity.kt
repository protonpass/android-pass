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

package proton.android.pass.data.impl.db.entities.securelinks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.user.data.entity.UserEntity
import proton.android.pass.data.impl.db.entities.ExternalColumns
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.db.entities.ShareEntity

@Entity(
    tableName = SecureLinkEntity.TABLE_NAME,
    primaryKeys = [SecureLinkEntity.Columns.LINK_ID],
    indices = [
        Index(value = [SecureLinkEntity.Columns.SHARE_ID]),
        Index(value = [SecureLinkEntity.Columns.SHARE_ID, SecureLinkEntity.Columns.ITEM_ID])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [SecureLinkEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = [ItemEntity.Columns.ID, ItemEntity.Columns.SHARE_ID],
            childColumns = [SecureLinkEntity.Columns.ITEM_ID, SecureLinkEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [SecureLinkEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SecureLinkEntity(
    @ColumnInfo(name = Columns.USER_ID, index = true)
    val userId: String,
    @ColumnInfo(name = Columns.LINK_ID)
    val linkId: String,
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String,
    @ColumnInfo(name = Columns.ITEM_ID)
    val itemId: String,
    @ColumnInfo(name = Columns.EXPIRATION)
    val expirationInSeconds: Long,
    @ColumnInfo(name = Columns.IS_ACTIVE)
    val isActive: Boolean,
    @ColumnInfo(name = Columns.MAX_VIEWS)
    val maxViews: Int?,
    @ColumnInfo(name = Columns.VIEWS)
    val views: Int,
    @ColumnInfo(name = Columns.URL)
    val url: String
) {
    internal companion object {

        const val TABLE_NAME = "SecureLinkEntity"

    }

    internal object Columns {

        internal const val LINK_ID = "link_id"

        internal const val USER_ID = "user_id"

        internal const val SHARE_ID = "share_id"

        internal const val ITEM_ID = "item_id"

        internal const val EXPIRATION = "expiration"

        internal const val IS_ACTIVE = "is_active"

        internal const val MAX_VIEWS = "max_views"

        internal const val VIEWS = "views"

        internal const val URL = "url"

    }

}
