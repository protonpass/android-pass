package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.isOrganizationAdmin
import me.proton.core.user.domain.extension.isOrganizationMember
import me.proton.core.user.domain.extension.isOrganizationUser
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.UserPlan
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class GetUserPlanImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val paymentManager: PaymentManager,
    private val getSubscription: GetCurrentSubscription
) : GetUserPlan {

    @Suppress("ReturnCount")
    override fun invoke(userId: UserId): Flow<UserPlan> = flow {
        val user = userRepository.getUser(userId)
        if (user.isOrganizationUser()) {
            emit(planForOrganizationUser(user))
        } else {
            emit(UserPlan.Free)
        }
    }

    private suspend fun planForOrganizationUser(user: User): UserPlan =
        if (user.isOrganizationAdmin()) {
            planForOrganizationAdmin(user)
        } else if (user.isOrganizationMember()) {
            UserPlan.Subuser
        } else {
            UserPlan.Free
        }
    private suspend fun planForOrganizationAdmin(user: User): UserPlan {
        val plan = getPlan(user.userId)
        return if (plan == null) {
            UserPlan.Free
        } else {
            UserPlan.Paid(
                internal = plan.name,
                humanReadable = plan.title
            )
        }
    }

    private suspend fun getPlan(userId: UserId): Plan? {
        val hasSubscription = paymentManager.isSubscriptionAvailable(userId)
        if (!hasSubscription) {
            return null
        }

        // If user does not have a subscription it means they are in the free plan
        val subscription = getSubscription.invoke(userId)
        if (subscription == null) {
            PassLogger.w(TAG, "PaymentManager has returned hasSubscription but is null")
            return null
        }
        return subscription.plans.firstOrNull { it.type == ACTIVE_PLAN_TYPE }
    }

    companion object {
        private const val TAG = "GetUserPlanImpl"
        private const val ACTIVE_PLAN_TYPE = 1
    }
}
