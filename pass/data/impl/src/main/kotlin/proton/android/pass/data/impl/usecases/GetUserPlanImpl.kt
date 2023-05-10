package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.impl.repositories.PlanRepository
import proton.pass.domain.PlanType
import javax.inject.Inject

class GetUserPlanImpl @Inject constructor(
    private val planRepository: PlanRepository
) : GetUserPlan {

    @Suppress("ReturnCount")
    override fun invoke(userId: UserId): Flow<PlanType> = planRepository.observePlanType(userId)
}
