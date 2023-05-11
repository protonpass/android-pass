package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.pass.domain.Item
import proton.pass.domain.ItemState
import proton.pass.domain.ShareSelection

interface ObserveItems {
    operator fun invoke(
        userId: UserId? = null,
        selection: ShareSelection,
        itemState: ItemState?,
        filter: ItemTypeFilter
    ): Flow<List<Item>>
}
