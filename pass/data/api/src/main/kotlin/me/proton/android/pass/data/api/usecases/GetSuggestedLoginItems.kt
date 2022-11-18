package me.proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item

interface GetSuggestedLoginItems {
    operator fun invoke(packageName: Option<String>, url: Option<String>): Flow<Result<List<Item>>>
}
