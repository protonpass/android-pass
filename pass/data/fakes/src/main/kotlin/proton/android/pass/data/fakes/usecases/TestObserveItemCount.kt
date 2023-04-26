package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.ObserveItemCount
import javax.inject.Inject

class TestObserveItemCount @Inject constructor() : ObserveItemCount {

    private val observeVaultsFlow = testFlow<Result<ItemCountSummary>>()

    fun sendResult(result: Result<ItemCountSummary>) = observeVaultsFlow.tryEmit(result)
    override fun invoke(): Flow<ItemCountSummary> = observeVaultsFlow.map {
        it.getOrThrow()
    }
}
