package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.pass.domain.Plan
import javax.inject.Inject

class TestGetUserPlan @Inject constructor() : GetUserPlan {

    private var result: MutableSharedFlow<Result<Plan>> = testFlow()

    fun setResult(value: Result<Plan>) {
        result.tryEmit(value)
    }

    override fun invoke(): Flow<Plan> = result.map {
        it.getOrThrow()
    }

    override fun invoke(userId: UserId) = invoke()
}
