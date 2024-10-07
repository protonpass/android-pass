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
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import proton.android.pass.data.impl.db.entities.AssetLinkEntity.Columns
import java.util.Date

@Entity(
    tableName = AssetLinkEntity.TABLE,
    indices = [
        Index(
            value = [Columns.WEBSITE, Columns.PACKAGE_NAME, Columns.SIGNATURE],
            unique = true
        )
    ]
)
@TypeConverters(DateConverter::class)
data class AssetLinkEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Columns.ID)
    val id: Long = 0,
    @ColumnInfo(name = Columns.WEBSITE)
    val website: String,
    @ColumnInfo(name = Columns.PACKAGE_NAME)
    val packageName: String,
    @ColumnInfo(name = Columns.CREATED_AT)
    val createdAt: Date,
    @ColumnInfo(name = Columns.SIGNATURE)
    val signature: String
) {
    object Columns {
        const val ID = "id"
        const val WEBSITE = "website"
        const val PACKAGE_NAME = "package_name"
        const val SIGNATURE = "signature"
        const val CREATED_AT = "created_at"
    }

    companion object {
        const val TABLE = "AssetLinkEntity"
    }
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let(::Date)

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
