package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.pass.domain.Item
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestGetSuggestedLoginItems @Inject constructor() : GetSuggestedLoginItems {

    private val resultFlow = testFlow<Result<List<Item>>>()

    fun sendValue(value: Result<List<Item>>) = resultFlow.tryEmit(value)

    override fun invoke(
        packageName: Option<String>,
        url: Option<String>
    ): Flow<List<Item>> = resultFlow
        .onEmpty { emit(Result.success(emptyList())) }
        .map {
            it.getOrThrow()
        }
}
