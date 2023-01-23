package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.map
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.Vault
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class ObserveVaultsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val cryptoContext: CryptoContext
) : ObserveVaults {

    override fun invoke(): Flow<Result<List<Vault>>> =
        accountManager.getPrimaryUserId()
            .flatMapLatest { primaryUserId ->
                if (primaryUserId != null) {
                    shareRepository.observeAllShares(primaryUserId)
                } else {
                    flowOf(Result.Error(UserIdNotAvailableError()))
                }
            }
            .map { result ->
                result.map { list ->
                    list.map { share ->
                        val content = share.content ?: throw ShareContentNotAvailableError()
                        val decrypted = cryptoContext.keyStoreCrypto.decrypt(content)
                        val parsed = VaultV1.Vault.parseFrom(decrypted.array)
                        Vault(share.id, parsed.name)
                    }
                }
            }
}
