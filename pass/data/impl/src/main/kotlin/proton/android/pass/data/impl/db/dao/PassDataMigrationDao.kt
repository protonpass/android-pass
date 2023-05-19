package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.PassDataMigrationEntity

@Dao
abstract class PassDataMigrationDao : BaseDao<PassDataMigrationEntity>() {
    @Query(
        """
        SELECT * FROM ${PassDataMigrationEntity.TABLE} 
        ORDER BY ${PassDataMigrationEntity.Columns.CREATE_TIME} ASC
        """
    )
    abstract suspend fun getExecutedMigrations(): List<PassDataMigrationEntity>
}
