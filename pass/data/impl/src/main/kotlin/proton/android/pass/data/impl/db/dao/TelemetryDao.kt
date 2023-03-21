package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.TelemetryEntity

@Dao
abstract class TelemetryDao : BaseDao<TelemetryEntity>() {

    @Query(
        """
        SELECT * FROM ${TelemetryEntity.TABLE} 
        WHERE ${TelemetryEntity.Columns.USER_ID} = :userId
        ORDER BY ${TelemetryEntity.Columns.CREATE_TIME} ASC
        """
    )
    abstract suspend fun getAll(userId: String): List<TelemetryEntity>

    @Query(
        """
        DELETE FROM ${TelemetryEntity.TABLE} 
        WHERE ${TelemetryEntity.Columns.ID} >= :min
          AND ${TelemetryEntity.Columns.ID} <= :max
        """
    )
    abstract suspend fun deleteInRange(min: Long, max: Long)
}
