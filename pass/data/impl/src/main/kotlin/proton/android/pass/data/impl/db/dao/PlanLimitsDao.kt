package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.PlanLimitsEntity

@Dao
abstract class PlanLimitsDao : BaseDao<PlanLimitsEntity>() {
    @Query(
        """
        SELECT * FROM ${PlanLimitsEntity.TABLE}
        WHERE ${PlanLimitsEntity.Columns.USER_ID} = :userId
        LIMIT 1
        """
    )
    abstract fun observeUserPlanLimits(userId: String): Flow<PlanLimitsEntity>
}
