package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.map
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.Vault
import javax.inject.Inject

class ObserveVaultsImpl @Inject constructor(
    private val observeAllShares: ObserveAllShares,
    private val encryptionContextProvider: EncryptionContextProvider,
) : ObserveVaults {

    override fun invoke(): Flow<LoadingResult<List<Vault>>> =
        observeAllShares()
            .map { result ->
                result.map { list ->
                    list.map { share ->
                        when (val res = share.toVault(encryptionContextProvider)) {
                            None -> throw ShareContentNotAvailableError()
                            is Some -> res.value
                        }
                    }
                }
            }

}
