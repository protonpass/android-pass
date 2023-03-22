package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetUserPlan

class TestGetUserPlan : GetUserPlan {

    private var result: Result<String> = Result.failure(IllegalStateException("value not set"))

    fun setResult(value: Result<String>) {
        result = value
    }

    override suspend fun invoke(userId: UserId): String = result.fold(
        onSuccess = { it },
        onFailure = { throw it }
    )
}
