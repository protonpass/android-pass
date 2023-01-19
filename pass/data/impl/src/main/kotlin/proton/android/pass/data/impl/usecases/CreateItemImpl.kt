package proton.android.pass.data.impl.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.CreateItem
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import javax.inject.Inject

class CreateItemImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : CreateItem {

    override suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents
    ): Result<Item> = when (val shareResult = shareRepository.getById(userId, shareId)) {
        is Result.Error -> Result.Error(shareResult.exception)
        Result.Loading -> Result.Loading
        is Result.Success -> {
            val share: Share? = shareResult.data
            if (share != null) {
                itemRepository.createItem(userId, share, itemContents)
            } else {
                Result.Error(IllegalStateException("CreateItem has invalid share"))
            }
        }
    }
}

