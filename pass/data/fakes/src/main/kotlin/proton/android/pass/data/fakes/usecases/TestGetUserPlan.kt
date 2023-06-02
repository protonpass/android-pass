package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.pass.domain.Plan
import proton.pass.domain.PlanLimit
import proton.pass.domain.PlanType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestGetUserPlan @Inject constructor() : GetUserPlan {

    private var result = MutableStateFlow(Result.success(DEFAULT_PLAN))

    fun setResult(value: Result<Plan>) {
        result.tryEmit(value)
    }

    override fun invoke(): Flow<Plan> = result.map { it.getOrThrow() }

    override fun invoke(userId: UserId) = invoke()

    companion object {
        val DEFAULT_PLAN = Plan(
            planType = PlanType.Free,
            hideUpgrade = false,
            vaultLimit = PlanLimit.Limited(10),
            aliasLimit = PlanLimit.Limited(10),
            totpLimit = PlanLimit.Limited(10),
            updatedAt = Clock.System.now().epochSeconds
        )
    }
}
