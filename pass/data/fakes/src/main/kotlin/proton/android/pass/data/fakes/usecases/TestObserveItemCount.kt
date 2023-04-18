package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.ObserveItemCount
import javax.inject.Inject

class TestObserveItemCount @Inject constructor() : ObserveItemCount {

    private val observeVaultsFlow: MutableSharedFlow<LoadingResult<ItemCountSummary>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun sendResult(result: LoadingResult<ItemCountSummary>) = observeVaultsFlow.tryEmit(result)
    override fun invoke(): Flow<LoadingResult<ItemCountSummary>> = observeVaultsFlow
}
