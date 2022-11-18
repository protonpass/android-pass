package me.proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item

interface ObserveTrashedItems {
    operator fun invoke(): Flow<Result<List<Item>>>
}
