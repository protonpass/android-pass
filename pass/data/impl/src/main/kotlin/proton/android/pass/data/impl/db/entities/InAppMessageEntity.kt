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
    tableName = InAppMessageEntity.TABLE,
    primaryKeys = [InAppMessageEntity.Columns.ID],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [InAppMessageEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InAppMessageEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String,
    @ColumnInfo(name = Columns.KEY)
    val key: String,
    @ColumnInfo(name = Columns.MODE)
    val mode: Int,
    @ColumnInfo(name = Columns.PRIORITY)
    val priority: Int,
    @ColumnInfo(name = Columns.TITLE)
    val title: String,
    @ColumnInfo(name = Columns.MESSAGE)
    val message: String?,
    @ColumnInfo(name = Columns.IMAGE_URL)
    val imageUrl: String?,
    @ColumnInfo(name = Columns.CTA_TEXT)
    val ctaText: String?,
    @ColumnInfo(name = Columns.CTA_ROUTE)
    val ctaRoute: String?,
    @ColumnInfo(name = Columns.CTA_TYPE)
    val ctaType: String?,
    @ColumnInfo(name = Columns.STATE)
    val state: Int,
    @ColumnInfo(name = Columns.RANGE_START)
    val rangeStart: Long,
    @ColumnInfo(name = Columns.RANGE_END)
    val rangeEnd: Long?,
    @ColumnInfo(name = Columns.USER_ID, index = true)
    val userId: String
) {
    object Columns {
        const val ID = "id"
        const val KEY = "key"
        const val MODE = "mode"
        const val PRIORITY = "priority"
        const val TITLE = "title"
        const val MESSAGE = "message"
        const val IMAGE_URL = "image_url"
        const val CTA_TEXT = "cta_text"
        const val CTA_ROUTE = "cta_route"
        const val CTA_TYPE = "cta_type"
        const val STATE = "state"
        const val RANGE_START = "range_start"
        const val RANGE_END = "range_end"
        const val USER_ID = "user_id"
    }

    companion object {
        const val TABLE = "InAppMessageEntity"
    }
}
