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

@Entity(
    tableName = SecureLinkTable.NAME,
    primaryKeys = [SecureLinkTable.Columns.LINK_ID]
)
data class SecureLinkEntity(
    @ColumnInfo(name = SecureLinkTable.Columns.USER_ID, index = true)
    val userId: String,
    @ColumnInfo(name = SecureLinkTable.Columns.LINK_ID)
    val linkId: String,
    @ColumnInfo(name = SecureLinkTable.Columns.SHARE_ID)
    val shareId: String,
    @ColumnInfo(name = SecureLinkTable.Columns.ITEM_ID)
    val itemId: String,
    @ColumnInfo(name = SecureLinkTable.Columns.EXPIRATION)
    val expirationInSeconds: Long,
    @ColumnInfo(name = SecureLinkTable.Columns.MAX_VIEWS)
    val maxViews: Int?,
    @ColumnInfo(name = SecureLinkTable.Columns.VIEWS)
    val views: Int,
    @ColumnInfo(name = SecureLinkTable.Columns.URL)
    val url: String
)
