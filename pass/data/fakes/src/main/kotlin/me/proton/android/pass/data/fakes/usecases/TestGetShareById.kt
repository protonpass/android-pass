package me.proton.android.pass.data.fakes.usecases

import me.proton.android.pass.data.api.usecases.GetShareById
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class TestGetShareById @Inject constructor() : GetShareById {

    private var result: Result<Share?> = Result.Loading

    fun setResult(result: Result<Share?>) {
        this.result = result
    }

    override suspend fun invoke(userId: UserId, shareId: ShareId): Result<Share?> = result
}
