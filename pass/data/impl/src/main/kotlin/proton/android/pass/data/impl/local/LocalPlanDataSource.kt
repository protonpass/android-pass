package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.dao.PlanTypeFields
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.responses.PlanResponse

interface LocalPlanDataSource {
    fun observePlan(userId: UserId): Flow<PlanEntity?>
    fun observePlanType(userId: UserId): Flow<PlanTypeFields>
    suspend fun storePlan(
        userId: UserId,
        planResponse: PlanResponse
    )
}
