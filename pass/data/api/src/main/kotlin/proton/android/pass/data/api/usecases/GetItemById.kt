package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface GetItemById {
    operator fun invoke(shareId: ShareId, itemId: ItemId): Flow<LoadingResult<Item>>
}
