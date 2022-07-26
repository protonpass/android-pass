package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository

class DeleteItem @Inject constructor(
    private val itemsRepository: ItemRepository
) {
    suspend operator fun invoke(userId: UserId, shareId: ShareId, itemId: ItemId) =
        itemsRepository.deleteItem(userId, shareId, itemId)
}
