package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.pass.domain.PlanType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanPerformPaidActionImpl @Inject constructor(
    private val getUserPlan: GetUserPlan,
) : CanPerformPaidAction {

    override fun invoke(): Flow<Boolean> = getUserPlan()
        .map { plan ->
            plan.planType is PlanType.Paid || plan.planType is PlanType.Trial
        }
}
