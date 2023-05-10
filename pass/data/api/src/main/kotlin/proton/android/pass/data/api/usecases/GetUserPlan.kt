package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.pass.domain.PlanType

interface GetUserPlan {
    operator fun invoke(userId: UserId): Flow<PlanType>
}
