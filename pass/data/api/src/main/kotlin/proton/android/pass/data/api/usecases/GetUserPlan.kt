package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.pass.domain.Plan

interface GetUserPlan {
    operator fun invoke(): Flow<Plan>
    operator fun invoke(userId: UserId): Flow<Plan>
}
