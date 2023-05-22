package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.PlanEntity

@Dao
abstract class PlanDao : BaseDao<PlanEntity>() {
    @Query(
        """
        SELECT * FROM ${PlanEntity.TABLE}
        WHERE ${PlanEntity.Columns.USER_ID} = :userId
        LIMIT 1
        """
    )
    abstract fun observeUserPlan(userId: String): Flow<PlanEntity>
}
