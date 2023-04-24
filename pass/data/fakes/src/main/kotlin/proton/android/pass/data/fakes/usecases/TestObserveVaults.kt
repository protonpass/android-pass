package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.Vault
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveVaults @Inject constructor() : ObserveVaults {

    private val observeVaultsFlow: MutableSharedFlow<Result<List<Vault>>> = testFlow()

    fun sendResult(result: Result<List<Vault>>) = observeVaultsFlow.tryEmit(result)
    override fun invoke(): Flow<List<Vault>> = observeVaultsFlow
        .map { it.getOrThrow() }
}
