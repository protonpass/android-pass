package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = SelectedShareEntity.TABLE,
    primaryKeys = [
        SelectedShareEntity.Columns.ID
    ],
    foreignKeys = [
        ForeignKey(
            entity = ShareEntity::class,
            parentColumns = [ShareEntity.Columns.ID],
            childColumns = [SelectedShareEntity.Columns.SHARE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SelectedShareEntity(
    @ColumnInfo(name = Columns.ID)
    val id: String = "selected_share",
    @ColumnInfo(name = Columns.SHARE_ID)
    val shareId: String
) {
    object Columns {
        const val ID = "id"
        const val SHARE_ID = "share_id"
    }

    companion object {
        const val TABLE = "SelectedShareEntity"
    }
}
