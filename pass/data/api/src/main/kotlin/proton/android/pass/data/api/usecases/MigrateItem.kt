package proton.android.pass.data.api.usecases

import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface MigrateItem {
    suspend operator fun invoke(
        sourceShare: ShareId,
        itemId: ItemId,
        destinationShare: ShareId
    ): Item
}
