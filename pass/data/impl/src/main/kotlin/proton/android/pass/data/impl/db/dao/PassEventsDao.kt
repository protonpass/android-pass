package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.impl.db.entities.PassEventEntity
import me.proton.core.data.room.db.BaseDao

@Dao
abstract class PassEventsDao : BaseDao<PassEventEntity>() {

    @Query(
        """
        SELECT * FROM ${PassEventEntity.TABLE}
        WHERE ${PassEventEntity.Columns.USER_ID} = :userId
          AND ${PassEventEntity.Columns.ADDRESS_ID} = :addressId
          AND ${PassEventEntity.Columns.SHARE_ID} = :shareId
        LIMIT 1
        """
    )
    abstract fun getLatestEventId(userId: String, addressId: String, shareId: String): Flow<PassEventEntity?>
}
