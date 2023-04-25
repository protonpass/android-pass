package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.ItemWithVaultInfo
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetItemByIdWithVaultImpl @Inject constructor(
    private val getItemById: GetItemById,
    private val observeVaults: ObserveVaults
) : GetItemByIdWithVault {
    override fun invoke(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<ItemWithVaultInfo> = getItemById(shareId, itemId).map { item ->
        val vaults = observeVaults().first()

        val hasMoreThanOneVault = vaults.size > 1
        val vault = vaults.firstOrNull { it.shareId == item.shareId }
        if (vault == null) {
            throw IllegalStateException("Vault not found")
        }

        ItemWithVaultInfo(
            item = item,
            vault = vault,
            hasMoreThanOneVault = hasMoreThanOneVault
        )
    }
}
