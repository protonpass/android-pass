package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.pass.domain.Item
import javax.inject.Inject

class TestGetSuggestedLoginItems @Inject constructor() : GetSuggestedLoginItems {

    override fun invoke(
        packageName: Option<String>,
        url: Option<String>
    ): Flow<LoadingResult<List<Item>>> = emptyFlow()
}
