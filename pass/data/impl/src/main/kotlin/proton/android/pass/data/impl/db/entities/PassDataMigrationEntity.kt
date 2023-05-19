package proton.android.pass.data.impl.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = PassDataMigrationEntity.TABLE,
    foreignKeys = []
)
data class PassDataMigrationEntity(
    @ColumnInfo(name = Columns.ID)
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = Columns.NAME)
    val name: String,
    @ColumnInfo(name = Columns.CREATE_TIME)
    val createTime: Long
) {
    object Columns {
        const val ID = "id"
        const val NAME = "name"
        const val CREATE_TIME = "create_time"
    }

    companion object {
        const val TABLE = "PassDataMigrationEntity"
    }
}
