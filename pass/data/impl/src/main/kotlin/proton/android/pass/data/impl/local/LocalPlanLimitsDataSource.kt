package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.PlanLimitsEntity

interface LocalPlanLimitsDataSource {
    fun observePlanLimits(userId: UserId): Flow<PlanLimitsEntity>
    suspend fun storePlanLimits(
        userId: UserId,
        vaultLimit: Int,
        aliasLimit: Int,
        totpLimit: Int
    )
}
