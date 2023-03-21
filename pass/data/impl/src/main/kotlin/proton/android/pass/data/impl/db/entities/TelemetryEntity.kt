package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import me.proton.core.user.data.entity.UserEntity

@Entity(
    tableName = TelemetryEntity.TABLE,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = [ExternalColumns.USER_ID],
            childColumns = [ShareEntity.Columns.USER_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TelemetryEntity(
    @ColumnInfo(name = Columns.ID)
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = Columns.USER_ID)
    val userId: String,
    @ColumnInfo(name = Columns.EVENT)
    val event: String,
    @ColumnInfo(name = Columns.DIMENSIONS)
    val dimensions: String,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long
) {
    object Columns {
        const val ID = "id"
        const val USER_ID = "user_id"
        const val EVENT = "event"
        const val DIMENSIONS = "dimensions"
        const val CREATE_TIME = "create_time"
    }

    companion object {
        const val TABLE = "TelemetryEntity"
    }
}
