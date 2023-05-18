package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.impl.db.dao.PlanTypeFields
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.local.LocalPlanDataSource
import proton.android.pass.data.impl.responses.PlanResponse

class TestLocalPlanDataSource : LocalPlanDataSource {

    private val planFlow = testFlow<PlanEntity?>()
    private val planTypeFieldsFlow = testFlow<PlanTypeFields>()
    private var storePlanResult: Result<Unit> = Result.success(Unit)

    fun emitPlan(planEntity: PlanEntity?) {
        planFlow.tryEmit(planEntity)
    }

    fun emitPlanTypeFields(planTypeFields: PlanTypeFields) {
        planTypeFieldsFlow.tryEmit(planTypeFields)
    }

    fun setStorePlanResult(result: Result<Unit>) {
        storePlanResult = result
    }

    override fun observePlan(userId: UserId): Flow<PlanEntity?> = planFlow

    override fun observePlanType(userId: UserId): Flow<PlanTypeFields> = planTypeFieldsFlow

    override suspend fun storePlan(userId: UserId, planResponse: PlanResponse) {
        storePlanResult.getOrThrow()
    }
}
