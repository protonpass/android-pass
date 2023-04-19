package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.RefreshContent
import javax.inject.Inject

class TestRefreshContent @Inject constructor() : RefreshContent {

    override suspend fun invoke(userId: UserId): LoadingResult<Unit> = LoadingResult.Success(Unit)
}
