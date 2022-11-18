package me.proton.android.pass.data.impl.usecases

import me.proton.android.pass.data.api.usecases.TrashItem
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class TrashItemImpl @Inject constructor(
    private val itemsRepository: me.proton.android.pass.data.api.repositories.ItemRepository
) : TrashItem {

    override suspend fun invoke(userId: UserId, shareId: ShareId, itemId: ItemId) =
        itemsRepository.trashItem(userId, shareId, itemId)
}

