package proton.android.pass.data.impl.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import proton.android.pass.data.api.usecases.GetUserPlan
import javax.inject.Inject

class GetUserPlanImpl @Inject constructor(
    private val getSubscription: GetCurrentSubscription
) : GetUserPlan {

    override suspend fun invoke(userId: UserId): String {
        // If user does not have a subscription it means they are in the free plan
        val subscription = getSubscription.invoke(userId) ?: return FREE_PLAN_NAME
        val plan = subscription
            .plans
            .firstOrNull { it.type == ACTIVE_PLAN_TYPE }
            ?: return FREE_PLAN_NAME

        return plan.title
    }

    companion object {
        private const val FREE_PLAN_NAME = "free"
        private const val ACTIVE_PLAN_TYPE = 1
    }
}
