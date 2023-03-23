package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.pass.domain.VaultWithItemCount
import javax.inject.Inject

class TestObserveVaultsWithItemCount @Inject constructor() : ObserveVaultsWithItemCount {

    private val observeVaultsFlow: MutableSharedFlow<LoadingResult<List<VaultWithItemCount>>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun sendResult(result: LoadingResult<List<VaultWithItemCount>>) = observeVaultsFlow.tryEmit(result)
    override fun invoke(): Flow<LoadingResult<List<VaultWithItemCount>>> = observeVaultsFlow
}
