package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.Vault
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class ObserveVaultsImpl @Inject constructor(
    private val observeAllShares: ObserveAllShares,
    private val encryptionContextProvider: EncryptionContextProvider
) : ObserveVaults {

    override fun invoke(): Flow<LoadingResult<List<Vault>>> =
        observeAllShares()
            .map { result ->
                result.map { list ->
                    list.map { share ->
                        val content = share.content ?: throw ShareContentNotAvailableError()
                        val decrypted = encryptionContextProvider.withEncryptionContext {
                            decrypt(content)
                        }
                        val parsed = VaultV1.Vault.parseFrom(decrypted)
                        Vault(share.id, parsed.name)
                    }
                }
            }
}
