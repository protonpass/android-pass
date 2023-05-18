package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.RemotePlanDataSource
import proton.android.pass.data.impl.responses.UserAccessResponse

class TestRemotePlanDataSource : RemotePlanDataSource {

    private var result: Result<UserAccessResponse> =
        Result.failure(IllegalStateException("TestRemotePlanDataSource not initialized"))

    fun setResult(value: Result<UserAccessResponse>) {
        result = value
    }

    override suspend fun sendUserAccessAndGetPlan(userId: UserId): UserAccessResponse =
        result.getOrThrow()
}
