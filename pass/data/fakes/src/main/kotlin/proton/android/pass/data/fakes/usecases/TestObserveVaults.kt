package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.Vault
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveVaults @Inject constructor() : ObserveVaults {

    private val observeVaultsFlow =
        MutableStateFlow<Result<List<Vault>>>(Result.success(emptyList()))

    fun sendResult(result: Result<List<Vault>>) = observeVaultsFlow.tryEmit(result)
    override fun invoke(): Flow<List<Vault>> = observeVaultsFlow
        .map { it.getOrThrow() }
}
