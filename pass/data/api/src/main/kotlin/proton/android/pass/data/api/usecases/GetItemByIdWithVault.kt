package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

data class ItemWithVaultInfo(
    val item: Item,
    val vault: Vault,
    val hasMoreThanOneVault: Boolean
)

interface GetItemByIdWithVault {
    operator fun invoke(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<ItemWithVaultInfo>
}
