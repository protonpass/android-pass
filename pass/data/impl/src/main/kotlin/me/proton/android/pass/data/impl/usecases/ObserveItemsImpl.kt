package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.usecases.ObserveItems
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveItemsImpl @Inject constructor(
    private val itemRepository: ItemRepository
) : ObserveItems {

    override fun invoke(
        userId: UserId,
        selection: ShareSelection,
        itemState: ItemState
    ): Flow<Result<List<Item>>> = itemRepository.observeItems(
        userId = userId,
        shareSelection = selection,
        itemState = itemState
    )
}

