package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface GetItemById {
    operator fun invoke(shareId: ShareId, itemId: ItemId): Flow<Item>
}
