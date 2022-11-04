package me.proton.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.repositories.ItemRepository

class TrashItem @Inject constructor(
    private val itemsRepository: ItemRepository
) {
    suspend operator fun invoke(userId: UserId, shareId: ShareId, itemId: ItemId) =
        itemsRepository.trashItem(userId, shareId, itemId)
}
