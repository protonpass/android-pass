package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.TrashItem
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TrashItemImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val itemsRepository: ItemRepository
) : TrashItem {

    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<Unit> =
        if (userId == null) {
            observeCurrentUser()
                .map { itemsRepository.trashItem(it.userId, shareId, itemId) }
                .first()
        } else {
            itemsRepository.trashItem(userId, shareId, itemId)
        }
}

