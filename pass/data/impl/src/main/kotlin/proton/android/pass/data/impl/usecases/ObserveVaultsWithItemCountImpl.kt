package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    override fun invoke(): Flow<List<VaultWithItemCount>> = observeVaults()
        .flatMapLatest { result -> observeItemCounts(result) }

    private fun observeItemCounts(
        vaultList: List<Vault>
    ): Flow<List<VaultWithItemCount>> = itemRepository.observeItemCount(
        shareIds = vaultList.map { it.shareId }
    ).map { count -> mapVaults(vaultList, count) }

    private fun mapVaults(
        vaultList: List<Vault>,
        count: Map<ShareId, ShareItemCount>
    ): List<VaultWithItemCount> {
        val res = vaultList.map { vault ->
            val itemsForShare = count[vault.shareId]
                ?: throw IllegalStateException("Could not find ItemCount for share")

            VaultWithItemCount(
                vault = vault,
                activeItemCount = itemsForShare.activeItems,
                trashedItemCount = itemsForShare.trashedItems
            )
        }.sorted()
        return res
    }

}
