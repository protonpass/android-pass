package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ObserveItems
import proton.pass.domain.Item
import proton.pass.domain.ItemState
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveItemsImpl @Inject constructor(
    private val itemRepository: ItemRepository
) : ObserveItems {

    override fun invoke(
        userId: UserId,
        selection: ShareSelection,
        itemState: ItemState
    ): Flow<List<Item>> = itemRepository.observeItems(
        userId = userId,
        shareSelection = selection,
        itemState = itemState
    )
}

