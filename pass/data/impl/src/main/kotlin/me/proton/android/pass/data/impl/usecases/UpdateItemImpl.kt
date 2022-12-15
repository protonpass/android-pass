package me.proton.android.pass.data.impl.usecases

import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.repositories.ShareRepository
import me.proton.android.pass.data.api.usecases.UpdateItem
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class UpdateItemImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : UpdateItem {

    override suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        item: Item,
        contents: ItemContents
    ): Result<Item> = when (val shareResult = shareRepository.getById(userId, shareId)) {
        is Result.Error -> Result.Error(shareResult.exception)
        Result.Loading -> Result.Loading
        is Result.Success -> {
            val share: Share? = shareResult.data
            if (share != null) {
                itemRepository.updateItem(userId, share, item, contents)
            } else {
                Result.Error(IllegalStateException("UpdateItem has invalid share"))
            }
        }
    }
}

