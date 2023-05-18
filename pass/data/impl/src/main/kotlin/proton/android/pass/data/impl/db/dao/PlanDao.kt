package proton.android.pass.data.impl.db.dao

import androidx.room.ColumnInfo
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

    @Query(
        """
        SELECT ${PlanEntity.Columns.TYPE}, 
        ${PlanEntity.Columns.INTERNAL_NAME}, 
        ${PlanEntity.Columns.DISPLAY_NAME} FROM ${PlanEntity.TABLE}
        WHERE ${PlanEntity.Columns.USER_ID} = :userId
        LIMIT 1
        """
    )
    abstract fun observeUserPlanType(userId: String): Flow<PlanTypeFields>
}

data class PlanTypeFields(
    @ColumnInfo(name = PlanEntity.Columns.TYPE)
    val type: String,
    @ColumnInfo(name = PlanEntity.Columns.INTERNAL_NAME)
    val internalName: String,
    @ColumnInfo(name = PlanEntity.Columns.DISPLAY_NAME)
    val displayName: String,
    @ColumnInfo(name = PlanEntity.Columns.TRIAL_END)
    val trialEnd: Long?
)
