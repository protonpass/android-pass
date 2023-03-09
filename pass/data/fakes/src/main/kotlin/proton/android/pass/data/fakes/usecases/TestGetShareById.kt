package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.GetShareById
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestGetShareById @Inject constructor() : GetShareById {

    private var result: LoadingResult<Share?> = LoadingResult.Loading

    fun setResult(result: LoadingResult<Share?>) {
        this.result = result
    }

    override suspend fun invoke(userId: UserId?, shareId: ShareId): LoadingResult<Share?> = result
}
