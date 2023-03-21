package proton.android.pass.data.impl.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.usecase.GetOrganization
import javax.inject.Inject

interface GetUserPlan {
    suspend operator fun invoke(userId: UserId): String
}

class GetUserPlanImpl @Inject constructor(
    private val getOrganization: GetOrganization
) : GetUserPlan {
    override suspend fun invoke(userId: UserId): String {
        val organization = getOrganization.invoke(userId, refresh = true)
        return organization?.planName ?: "free"
    }
}
