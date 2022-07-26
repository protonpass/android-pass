package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.repositories.ItemRepository

class ObserveItems @Inject constructor(
    private val itemRepository: ItemRepository
) {
    operator fun invoke(userId: UserId, selection: ShareSelection): Flow<List<Item>> =
        itemRepository.observeItems(userId, selection)
}
