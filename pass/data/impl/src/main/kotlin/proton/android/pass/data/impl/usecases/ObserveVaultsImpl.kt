package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.Vault
import proton.pass.domain.sorted
import javax.inject.Inject

class ObserveVaultsImpl @Inject constructor(
    private val observeAllShares: ObserveAllShares,
    private val encryptionContextProvider: EncryptionContextProvider,
) : ObserveVaults {

    override fun invoke(): Flow<List<Vault>> = observeAllShares()
        .map { result ->
            when (result) {
                LoadingResult.Loading -> emptyList()
                is LoadingResult.Error -> throw result.exception
                is LoadingResult.Success -> {
                    val list = result.data
                    list.map { share ->
                        when (val res = share.toVault(encryptionContextProvider)) {
                            None -> throw ShareContentNotAvailableError()
                            is Some -> res.value
                        }
                    }.sorted()
                }
            }
        }

}
