package proton.android.pass.data.impl.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.usersettings.domain.usecase.GetOrganization
import proton.android.pass.data.api.usecases.GetUserPlan
import javax.inject.Inject

class GetUserPlanImpl @Inject constructor(
    private val getOrganization: GetOrganization
) : GetUserPlan {

    override suspend fun invoke(userId: UserId): String {
        val organization = getOrganization.invoke(userId, refresh = true)

        // If the user does not have an organization it means that they are not in a paid plan
        return organization?.planName ?: "free"
    }
}
