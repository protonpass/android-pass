package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.pass.domain.Item
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestGetSuggestedLoginItems @Inject constructor() : GetSuggestedLoginItems {

    private val resultFlow = MutableStateFlow<Result<List<Item>>>(Result.success(emptyList()))

    fun sendValue(value: Result<List<Item>>) = resultFlow.tryEmit(value)

    override fun invoke(
        packageName: Option<String>,
        url: Option<String>
    ): Flow<List<Item>> = resultFlow.map { it.getOrThrow() }
}
