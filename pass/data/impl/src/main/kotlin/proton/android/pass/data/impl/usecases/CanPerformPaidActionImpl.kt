package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
        .flatMapLatest { plan ->
            when (plan.planType) {
                is PlanType.Paid, is PlanType.Trial -> flowOf(true)
                else -> flowOf(false)
            }
        }
}
