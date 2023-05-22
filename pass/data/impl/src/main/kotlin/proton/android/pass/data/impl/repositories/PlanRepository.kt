package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.pass.domain.Plan

interface PlanRepository {
    fun sendUserAccessAndObservePlan(userId: UserId, forceRefresh: Boolean): Flow<Plan>
    fun observePlan(userId: UserId): Flow<Plan>
}
