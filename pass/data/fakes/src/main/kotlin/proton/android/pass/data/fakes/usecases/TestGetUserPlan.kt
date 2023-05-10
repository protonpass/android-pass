package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.pass.domain.PlanType
import javax.inject.Inject

class TestGetUserPlan @Inject constructor() : GetUserPlan {

    private var result: MutableSharedFlow<Result<PlanType>> = testFlow()

    fun setResult(value: Result<PlanType>) {
        result.tryEmit(value)
    }

    override fun invoke(userId: UserId) = result.map {
        it.getOrThrow()
    }
}
