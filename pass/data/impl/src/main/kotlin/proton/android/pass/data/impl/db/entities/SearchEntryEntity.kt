package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = SearchEntryEntity.TABLE,
    primaryKeys = [SearchEntryEntity.Columns.SHARE_ID, SearchEntryEntity.Columns.ITEM_ID],
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = [ItemEntity.Columns.ID, ItemEntity.Columns.SHARE_ID],
            childColumns = [SearchEntryEntity.Columns.ITEM_ID, SearchEntryEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SearchEntryEntity(
    @ColumnInfo(name = Columns.ITEM_ID)
    val itemId: String,
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long
) {
    object Columns {
        const val ITEM_ID = "item_id"
        const val SHARE_ID = "share_id"
        const val USER_ID = "user_id"
        const val CREATE_TIME = "create_time"
    }

    companion object {
        const val TABLE = "SearchEntryEntity"
    }
}
