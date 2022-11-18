package me.proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ShareId

interface ObserveActiveShare {
    operator fun invoke(): Flow<Result<ShareId?>>
}
