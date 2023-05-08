package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.UserPlan
import proton.android.pass.data.impl.api.PasswordManagerApi
import javax.inject.Inject

class GetUserPlanImpl @Inject constructor(
    private val api: ApiProvider,
) : GetUserPlan {

    @Suppress("ReturnCount")
    override fun invoke(userId: UserId): Flow<UserPlan> = flow {
        val response = api.get<PasswordManagerApi>(userId)
            .invoke { userAccess() }
            .valueOrThrow

        val plan = response.accessResponse.planResponse
        val userPlan = if (plan.type == FREE_PLAN_TYPE) {
            UserPlan.Free
        } else {
            UserPlan.Paid(internal = plan.internalName, humanReadable = plan.displayName)
        }

        emit(userPlan)
    }

    companion object {
        private const val FREE_PLAN_TYPE = "free"
    }
}
