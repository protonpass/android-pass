package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.common.api.Result
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.Vault
import javax.inject.Inject

class TestObserveVaults @Inject constructor() : ObserveVaults {

    private val observeVaultsFlow: MutableSharedFlow<Result<List<Vault>>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun sendResult(result: Result<List<Vault>>) = observeVaultsFlow.tryEmit(result)
    override fun invoke(): Flow<Result<List<Vault>>> = observeVaultsFlow
}
