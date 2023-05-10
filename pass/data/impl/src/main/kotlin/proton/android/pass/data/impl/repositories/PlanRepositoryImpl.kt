package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.dao.PlanTypeFields
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.local.LocalPlanDataSource
import proton.android.pass.data.impl.remote.RemotePlanDataSource
import proton.pass.domain.Plan
import proton.pass.domain.PlanType
import javax.inject.Inject

class PlanRepositoryImpl @Inject constructor(
    private val remotePlanDataSource: RemotePlanDataSource,
    private val localPlanDataSource: LocalPlanDataSource
) : PlanRepository {

    override fun sendUserAccessAndObservePlan(userId: UserId, forceRefresh: Boolean): Flow<Plan> =
        localPlanDataSource.observePlan(userId)
            .map {
                Plan(
                    planType = it.toPlanType(),
                    vaultLimit = it.vaultLimit,
                    aliasLimit = it.aliasLimit,
                    totpLimit = it.totpLimit
                )
            }
            .onStart {
                if (forceRefresh) {
                    runCatching {
                        val response = remotePlanDataSource.sendUserAccessAndGetPlan(userId)
                        localPlanDataSource.storePlan(
                            userId = userId,
                            planResponse = response.accessResponse.planResponse
                        )
                    }
                }
            }

    override fun observePlanType(userId: UserId): Flow<PlanType> =
        localPlanDataSource.observePlanType(userId)
            .map { it.toPlanType() }

    private fun PlanTypeFields.toPlanType(): PlanType = when (type) {
        PlanType.Free.internalName() -> PlanType.Free
        else -> PlanType.Paid(
            internal = internalName,
            humanReadable = displayName
        )
    }

    private fun PlanEntity.toPlanType(): PlanType = when (type) {
        PlanType.Free.internalName() -> PlanType.Free
        else -> PlanType.Paid(
            internal = internalName,
            humanReadable = displayName
        )
    }
}
