package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Result
import proton.pass.domain.ShareId

interface ObserveActiveShare {
    operator fun invoke(): Flow<Result<ShareId?>>
}
