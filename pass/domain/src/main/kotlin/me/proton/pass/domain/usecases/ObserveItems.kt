package me.proton.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ShareSelection
import me.proton.pass.domain.repositories.ItemRepository
import javax.inject.Inject

class ObserveItems @Inject constructor(
    private val itemRepository: ItemRepository
) {
    operator fun invoke(userId: UserId, selection: ShareSelection, itemState: ItemState): Flow<Result<List<Item>>> =
        itemRepository.observeItems(userId, selection, itemState)
}
