package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.ShareId

interface ObserveActiveShare {
    operator fun invoke(userId: UserId? = null): Flow<Result<ShareId>>
}
