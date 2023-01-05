package me.proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import javax.inject.Inject

class TestGetSuggestedLoginItems @Inject constructor() : GetSuggestedLoginItems {

    override fun invoke(
        packageName: Option<String>,
        url: Option<String>
    ): Flow<Result<List<Item>>> = emptyFlow()
}
