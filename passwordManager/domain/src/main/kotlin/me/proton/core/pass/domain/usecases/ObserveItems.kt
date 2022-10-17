package me.proton.core.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemState
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.repositories.ItemRepository
import javax.inject.Inject

class ObserveItems @Inject constructor(
    private val itemRepository: ItemRepository
) {
    operator fun invoke(userId: UserId, selection: ShareSelection, itemState: ItemState): Flow<Result<List<Item>>> =
        itemRepository.observeItems(userId, selection, itemState)
}
