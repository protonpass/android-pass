package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetShareById
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestGetShareById @Inject constructor() : GetShareById {

    private var result: Result<Share> = Result.failure(IllegalStateException("Result not set"))

    fun setResult(result: Result<Share>) {
        this.result = result
    }

    override suspend fun invoke(userId: UserId?, shareId: ShareId): Share = result.getOrThrow()
}
