package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.repositories.ItemRepository

class CreateItem @Inject constructor(
    private val itemRepository: ItemRepository
) {
    suspend fun invoke(userId: UserId, share: Share, contents: ItemContents) =
        itemRepository.createItem(userId, share, contents)
}
