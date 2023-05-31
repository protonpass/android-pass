package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.pass.domain.VaultWithItemCount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveVaultsWithItemCount @Inject constructor() : ObserveVaultsWithItemCount {

    private val observeVaultsFlow: MutableSharedFlow<Result<List<VaultWithItemCount>>> = testFlow()

    fun sendResult(result: Result<List<VaultWithItemCount>>) = observeVaultsFlow.tryEmit(result)

    override fun invoke(): Flow<List<VaultWithItemCount>> = observeVaultsFlow.map {
        it.getOrThrow()
    }
}
