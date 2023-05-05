package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.PlanLimitsEntity
import javax.inject.Inject

class LocalPlanLimitsDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalPlanLimitsDataSource {
    override fun observePlanLimits(userId: UserId): Flow<PlanLimitsEntity> =
        database.planLimitsDao().observeUserPlanLimits(userId.id)

    override suspend fun storePlanLimits(
        userId: UserId,
        vaultLimit: Int,
        aliasLimit: Int,
        totpLimit: Int,
    ) {
        val entity = PlanLimitsEntity(
            userId = userId.id,
            vaultLimit = vaultLimit,
            aliasLimit = aliasLimit,
            totpLimit = totpLimit
        )
        database.planLimitsDao().insertOrUpdate(entity)
    }
}
