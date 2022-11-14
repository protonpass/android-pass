package me.proton.pass.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.domain.repositories.ItemRepository
import me.proton.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class CreateItemImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : CreateItem {

    override suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents,
        packageName: PackageName?
    ): Result<Item> = when (val shareResult = shareRepository.getById(userId, shareId)) {
        is Result.Error -> Result.Error(shareResult.exception)
        Result.Loading -> Result.Loading
        is Result.Success -> {
            val share: Share? = shareResult.data
            if (share != null) {
                itemRepository.createItem(userId, share, itemContents, packageName)
            } else {
                Result.Error(IllegalStateException("CreateItem has invalid share"))
            }
        }
    }
}

interface CreateItem {
    suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents,
        packageName: PackageName? = null
    ): Result<Item>
}
