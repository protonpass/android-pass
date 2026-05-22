/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.local.search

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = SearchItemEntity.TABLE,
    indices = [
        Index(value = [SearchItemEntity.Columns.USER_ID]),
        Index(value = [SearchItemEntity.Columns.SHARE_ID]),
        Index(value = [SearchItemEntity.Columns.FOLDER_ID]),
        Index(value = [SearchItemEntity.Columns.ITEM_ID]),
        Index(value = [SearchItemEntity.Columns.TITLE]),
        Index(value = [SearchItemEntity.Columns.CREATE_TIME]),
        Index(value = [SearchItemEntity.Columns.MODIFY_TIME]),
        Index(value = [SearchItemEntity.Columns.ITEM_STATE]),
        Index(value = [SearchItemEntity.Columns.IS_SHARED_BY_ME]),
        Index(value = [SearchItemEntity.Columns.IS_SHARED_WITH_ME]),
        Index(value = [SearchItemEntity.Columns.LAST_AUTOFILL_TIME]),
        Index(value = [SearchItemEntity.Columns.HAS_TOTP]),
        Index(value = [SearchItemEntity.Columns.IS_HIDDEN]),
        Index(
            value = [
                SearchItemEntity.Columns.USER_ID,
                SearchItemEntity.Columns.SHARE_ID,
                SearchItemEntity.Columns.ITEM_ID
            ],
            unique = true
        )
    ]
)
data class SearchItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Columns.ROW_ID)
    val rowId: Long = 0,

    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,

    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String,

    @ColumnInfo(name = Columns.FOLDER_ID)
    val folderId: String? = null,

    @ColumnInfo(name = Columns.ITEM_ID)
    val itemId: String,

    @ColumnInfo(name = Columns.TITLE)
    val title: String,

    @ColumnInfo(name = Columns.SUBTITLE)
    val subtitle: String?,

    @ColumnInfo(name = Columns.ITEM_TYPE)
    val itemType: Int,

    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long,

    @ColumnInfo(name = Columns.MODIFY_TIME)
    val modifyTime: Long,

    @ColumnInfo(name = Columns.ITEM_STATE, defaultValue = "0")
    val itemState: Int = 0, // 0 = Active, 1 = Trashed

    @ColumnInfo(name = Columns.IS_SHARED_BY_ME, defaultValue = "0")
    val isSharedByMe: Boolean = false,

    @ColumnInfo(name = Columns.IS_SHARED_WITH_ME, defaultValue = "0")
    val isSharedWithMe: Boolean = false,

    @ColumnInfo(name = Columns.LAST_AUTOFILL_TIME)
    val lastAutofillTime: Long? = null,

    @ColumnInfo(name = Columns.HAS_TOTP, defaultValue = "0")
    val hasTotp: Boolean = false,

    @ColumnInfo(name = Columns.IS_HIDDEN, defaultValue = "0")
    val isHidden: Boolean = false
) {
    object Columns {
        const val ROW_ID = "rowId"
        const val USER_ID = "user_id"
        const val SHARE_ID = "share_id"
        const val FOLDER_ID = "folder_id"
        const val ITEM_ID = "item_id"
        const val TITLE = "title"
        const val SUBTITLE = "subtitle"
        const val ITEM_TYPE = "item_type"
        const val CREATE_TIME = "create_time"
        const val MODIFY_TIME = "modify_time"
        const val ITEM_STATE = "item_state"
        const val IS_SHARED_BY_ME = "is_shared_by_me"
        const val IS_SHARED_WITH_ME = "is_shared_with_me"
        const val LAST_AUTOFILL_TIME = "last_autofill_time"
        const val HAS_TOTP = "has_totp"
        const val IS_HIDDEN = "is_hidden"
    }

    companion object {
        const val TABLE = "search_items"
    }
}
