package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Item

interface GetSuggestedLoginItems {
    operator fun invoke(packageName: Option<String>, url: Option<String>): Flow<LoadingResult<List<Item>>>
}
