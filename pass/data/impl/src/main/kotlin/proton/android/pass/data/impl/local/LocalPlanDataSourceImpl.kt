package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.dao.PlanTypeFields
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.responses.PlanResponse
import javax.inject.Inject

class LocalPlanDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    private val clock: Clock
) : LocalPlanDataSource {

    override fun observePlan(userId: UserId): Flow<PlanEntity> =
        database.planDao().observeUserPlan(userId.id)

    override fun observePlanType(userId: UserId): Flow<PlanTypeFields> =
        database.planDao().observeUserPlanType(userId.id)

    override suspend fun storePlan(userId: UserId, planResponse: PlanResponse) {
        val entity = PlanEntity(
            userId = userId.id,
            vaultLimit = planResponse.vaultLimit ?: -1,
            aliasLimit = planResponse.aliasLimit ?: -1,
            totpLimit = planResponse.totpLimit ?: -1,
            type = planResponse.type,
            internalName = planResponse.internalName,
            displayName = planResponse.displayName,
            updatedAt = clock.now().epochSeconds
        )
        database.planDao().insertOrUpdate(entity)
    }
}
