package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Result
import proton.pass.domain.Item

interface ObserveTrashedItems {
    operator fun invoke(): Flow<Result<List<Item>>>
}
