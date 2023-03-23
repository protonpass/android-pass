package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount
import proton.pass.domain.sorted
import javax.inject.Inject

class ObserveVaultsWithItemCountImpl @Inject constructor(
    private val observeVaults: ObserveVaults,
    private val itemRepository: ItemRepository
) : ObserveVaultsWithItemCount {

    override fun invoke(): Flow<LoadingResult<List<VaultWithItemCount>>> = observeVaults()
        .flatMapLatest { result ->
            when (result) {
                LoadingResult.Loading -> flowOf(LoadingResult.Loading)
                is LoadingResult.Error -> flowOf(LoadingResult.Error(result.exception))
                is LoadingResult.Success -> observeItemCounts(result.data)
            }
        }

    private fun observeItemCounts(
        vaultList: List<Vault>
    ): Flow<LoadingResult<List<VaultWithItemCount>>> = itemRepository.observeItemCount(
        shareIds = vaultList.map { it.shareId }
    ).map { count -> mapVaults(vaultList, count) }

    private fun mapVaults(
        vaultList: List<Vault>,
        count: Map<ShareId, ShareItemCount>
    ): LoadingResult<List<VaultWithItemCount>> {
        val res = vaultList.map { vault ->
            val itemsForShare = count[vault.shareId]
                ?: return LoadingResult.Error(IllegalStateException("Could not find ItemCount for share"))

            VaultWithItemCount(
                vault = vault,
                activeItemCount = itemsForShare.activeItems,
                trashedItemCount = itemsForShare.trashedItems
            )
        }.sorted()
        return LoadingResult.Success(res)
    }

}
