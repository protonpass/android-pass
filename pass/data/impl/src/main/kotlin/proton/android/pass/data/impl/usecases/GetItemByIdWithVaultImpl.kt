package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.ItemWithVaultInfo
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.log.api.PassLogger
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
    ): Flow<ItemWithVaultInfo> = getItemById(shareId, itemId).map { res ->
        val item = when (res) {
            LoadingResult.Loading -> throw IllegalStateException("LoadingResult.Loading should not be emitted")
            is LoadingResult.Error -> {
                PassLogger.e(TAG, res.exception, "Error loading item")
                throw res.exception
            }
            is LoadingResult.Success -> res.data
        }
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


    companion object {
        private const val TAG = "GetItemByIdWithVaultImpl"
    }
}
