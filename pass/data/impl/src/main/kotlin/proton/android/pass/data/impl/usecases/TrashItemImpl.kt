package proton.android.pass.data.impl.usecases

import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.TrashItem
import me.proton.core.domain.entity.UserId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TrashItemImpl @Inject constructor(
    private val itemsRepository: ItemRepository
) : TrashItem {

    override suspend fun invoke(userId: UserId, shareId: ShareId, itemId: ItemId) =
        itemsRepository.trashItem(userId, shareId, itemId)
}

