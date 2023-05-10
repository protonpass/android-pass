package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.pass.domain.Plan
import proton.pass.domain.PlanType

interface PlanRepository {
    fun sendUserAccessAndObservePlan(userId: UserId, forceRefresh: Boolean): Flow<Plan>
    fun observePlanType(userId: UserId): Flow<PlanType>
}
