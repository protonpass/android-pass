package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.ShareId

interface ObserveActiveShare {
    operator fun invoke(userId: UserId? = null): Flow<LoadingResult<ShareId>>
}
