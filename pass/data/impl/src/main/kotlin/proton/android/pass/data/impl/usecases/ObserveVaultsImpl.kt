package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class ObserveVaultsImpl @Inject constructor(
    private val observeAllShares: ObserveAllShares,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val itemRepository: ItemRepository
) : ObserveVaults {

    override fun invoke(): Flow<LoadingResult<List<Vault>>> =
        observeAllShares()
            .flatMapLatest { result ->
                when (result) {
                    LoadingResult.Loading -> flowOf(LoadingResult.Loading)
                    is LoadingResult.Error -> flowOf(LoadingResult.Error(result.exception))
                    is LoadingResult.Success -> {
                        observeVaults(result.data)
                    }
                }
            }

    private fun observeVaults(shareList: List<Share>): Flow<LoadingResult<List<Vault>>> =
        itemRepository.observeItemCount(
            shareIds = shareList.map { it.id }
        ).map { count -> mapVaults(shareList, count) }

    private fun mapVaults(
        shareList: List<Share>,
        count: Map<ShareId, ShareItemCount>
    ): LoadingResult<List<Vault>> {
        val res = shareList.map { share ->
            when (val content = share.content) {
                is Some -> {
                    val itemsForShare = count[share.id]
                        ?: return LoadingResult.Error(IllegalStateException("Could not find ItemCount for share"))

                    val decrypted = encryptionContextProvider.withEncryptionContext {
                        decrypt(content.value)
                    }
                    val parsed = VaultV1.Vault.parseFrom(decrypted)
                    Vault(
                        shareId = share.id,
                        name = parsed.name,
                        activeItemCount = itemsForShare.activeItems,
                        trashedItemCount = itemsForShare.trashedItems
                    )
                }
                None -> throw ShareContentNotAvailableError()
            }
        }
        return LoadingResult.Success(res)
    }

}
