package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.dao.PlanTypeFields
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.local.LocalPlanDataSource
import proton.android.pass.data.impl.remote.RemotePlanDataSource
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.Plan
import proton.pass.domain.PlanType
import javax.inject.Inject

class PlanRepositoryImpl @Inject constructor(
    private val remotePlanDataSource: RemotePlanDataSource,
    private val localPlanDataSource: LocalPlanDataSource,
    private val clock: Clock
) : PlanRepository {

    override fun sendUserAccessAndObservePlan(userId: UserId, forceRefresh: Boolean): Flow<Plan> =
        localPlanDataSource.observePlan(userId)
            .mapNotNull { planEntity ->
                if (planEntity == null) {
                    refreshPlan(userId)
                    null
                } else {
                    val difference =
                        clock.now().minus(Instant.fromEpochSeconds(planEntity.updatedAt))
                    if (difference.inWholeDays >= 1) {
                        refreshPlan(userId)
                    }
                    Plan(
                        planType = planEntity.toPlanType(),
                        vaultLimit = planEntity.vaultLimit,
                        aliasLimit = planEntity.aliasLimit,
                        totpLimit = planEntity.totpLimit,
                        updatedAt = planEntity.updatedAt
                    )
                }
            }
            .onStart {
                if (forceRefresh) {
                    refreshPlan(userId)
                }
            }

    override fun observePlanType(userId: UserId): Flow<PlanType> =
        localPlanDataSource.observePlanType(userId)
            .filterNotNull()
            .map { it.toPlanType() }

    private suspend fun refreshPlan(userId: UserId) {
        runCatching {
            val response = remotePlanDataSource.sendUserAccessAndGetPlan(userId)
            localPlanDataSource.storePlan(
                userId = userId,
                planResponse = response.accessResponse.planResponse
            )
        }
            .onSuccess {
                PassLogger.i(TAG, "Plan refreshed")
            }
            .onFailure {
                PassLogger.w(TAG, it, "Plan failed to refresh")
            }
    }

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

    companion object {
        private const val TAG = "PlanRepositoryImpl"
    }
}
