package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.pass.domain.Plan
import proton.pass.domain.PlanType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestGetUserPlan @Inject constructor() : GetUserPlan {

    private var result: MutableSharedFlow<Result<Plan>> = testFlow()

    fun setResult(value: Result<Plan>) {
        result.tryEmit(value)
    }

    override fun invoke(): Flow<Plan> = result
        .onEmpty { emit(Result.success(DEFAULT_PLAN)) }
        .map {
            it.getOrThrow()
        }

    override fun invoke(userId: UserId) = invoke()

    companion object {
        val DEFAULT_PLAN = Plan(
            planType = PlanType.Free,
            hideUpgrade = false,
            vaultLimit = 10,
            aliasLimit = 10,
            totpLimit = 10,
            updatedAt = Clock.System.now().epochSeconds
        )
    }
}
