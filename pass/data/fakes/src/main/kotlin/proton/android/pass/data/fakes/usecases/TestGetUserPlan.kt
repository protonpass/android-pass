package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.UserPlan
import javax.inject.Inject

class TestGetUserPlan @Inject constructor() : GetUserPlan {

    private var result: Result<UserPlan> = Result.failure(IllegalStateException("value not set"))

    fun setResult(value: Result<UserPlan>) {
        result = value
    }

    override suspend fun invoke(userId: UserId): UserPlan = result.fold(
        onSuccess = { it },
        onFailure = { throw it }
    )
}
