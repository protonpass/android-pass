package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItems
import proton.pass.domain.Item
import proton.pass.domain.ItemState
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveItemsImpl @Inject constructor(
    private val itemRepository: ItemRepository,
    private val observeCurrentUser: ObserveCurrentUser,
) : ObserveItems {

    override fun invoke(
        userId: UserId?,
        selection: ShareSelection,
        itemState: ItemState,
        filter: ItemTypeFilter
    ): Flow<List<Item>> =
        if (userId == null) {
            observeCurrentUser()
                .flatMapLatest {
                    observeItems(it.userId, selection, itemState, filter)
                }
        } else {
            observeItems(userId, selection, itemState, filter)
        }

    private fun observeItems(
        userId: UserId,
        selection: ShareSelection,
        itemState: ItemState,
        filter: ItemTypeFilter
    ): Flow<List<Item>> =
        itemRepository.observeItems(
            userId = userId,
            shareSelection = selection,
            itemState = itemState,
            itemTypeFilter = filter
        )
}

