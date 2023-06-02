package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.local.LocalPlanDataSource
import proton.android.pass.data.impl.remote.RemotePlanDataSource
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.Plan
import proton.pass.domain.PlanLimit
import proton.pass.domain.PlanType
import java.lang.Math.ceil
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
                    val planUpdatedAt = Instant.fromEpochSeconds(planEntity.updatedAt)
                    val difference = clock.now().minus(planUpdatedAt)
                    if (difference.inWholeDays >= 1) {
                        refreshPlan(userId)
                    }
                    planEntity.toPlan()
                }
            }
            .onStart {
                if (forceRefresh) {
                    refreshPlan(userId)
                }
            }

    override fun observePlan(userId: UserId): Flow<Plan> =
        localPlanDataSource.observePlan(userId)
            .filterNotNull()
            .map { it.toPlan() }


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

    private fun getTrialStatus(trialEnd: Long?): TrialStatus {
        if (trialEnd != null) {
            val parsedTrial = Instant.fromEpochSeconds(trialEnd)
            val now = clock.now()
            val isInTrial = parsedTrial > now
            if (isInTrial) {
                val remainingHours = (parsedTrial - now).inWholeHours
                val days = remainingHours / 24f
                val daysAsInt = ceil(days.toDouble())

                return TrialStatus.Trial(daysAsInt.toInt())
            }
        }

        return TrialStatus.NotTrial
    }

    private fun PlanEntity.toPlan(): Plan {
        val plan = if (trialEnd != null) {
            when (val trial = getTrialStatus(trialEnd)) {
                TrialStatus.NotTrial -> toPlanType(false)
                is TrialStatus.Trial -> toPlanType(true, trial.remainingDays)
            }
        } else {
            toPlanType(false)
        }
        return Plan(
            planType = plan,
            vaultLimit = toPlanLimit(vaultLimit),
            aliasLimit = toPlanLimit(aliasLimit),
            totpLimit = toPlanLimit(totpLimit),
            updatedAt = updatedAt,
            hideUpgrade = hideUpgrade
        )
    }

    private fun PlanEntity.toPlanType(isTrial: Boolean, remainingTrialDays: Int = 0): PlanType =
        when (type) {
            PlanType.PLAN_NAME_FREE -> if (isTrial) {
                PlanType.Trial(
                    internal = internalName,
                    humanReadable = displayName,
                    remainingDays = remainingTrialDays
                )
            } else {
                PlanType.Free
            }

            PlanType.PLAN_NAME_PLUS -> if (isTrial) {
                PlanType.Trial(
                    internal = internalName,
                    humanReadable = displayName,
                    remainingDays = remainingTrialDays
                )
            } else {
                PlanType.Paid(
                    internal = internalName,
                    humanReadable = displayName
                )
            }

            else -> PlanType.Unknown(internal = internalName, humanReadable = displayName)
        }

    private fun toPlanLimit(value: Int): PlanLimit = if (value == -1) {
        PlanLimit.Unlimited
    } else {
        PlanLimit.Limited(value)
    }

    internal sealed interface TrialStatus {
        object NotTrial : TrialStatus
        data class Trial(val remainingDays: Int) : TrialStatus
    }

    companion object {
        private const val TAG = "PlanRepositoryImpl"
    }
}
