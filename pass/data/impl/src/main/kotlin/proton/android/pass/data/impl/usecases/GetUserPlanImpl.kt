package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.impl.repositories.PlanRepository
import proton.pass.domain.Plan
import javax.inject.Inject

class GetUserPlanImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val planRepository: PlanRepository
) : GetUserPlan {

    override fun invoke(): Flow<Plan> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { invoke(it) }

    override fun invoke(userId: UserId): Flow<Plan> = planRepository.observePlan(userId)
}
