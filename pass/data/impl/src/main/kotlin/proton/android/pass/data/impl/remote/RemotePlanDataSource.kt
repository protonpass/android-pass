package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.responses.UserAccessResponse

interface RemotePlanDataSource {
    suspend fun sendUserAccessAndGetPlan(userId: UserId): UserAccessResponse
}
