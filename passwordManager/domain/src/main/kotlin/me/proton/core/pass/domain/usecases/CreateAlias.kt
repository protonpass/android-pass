package me.proton.core.pass.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.NewAlias
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class CreateAlias @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        newAlias: NewAlias
    ): Result<Item> = when (val shareResult = shareRepository.getById(userId, shareId)) {
        is Result.Error -> Result.Error(shareResult.exception)
        Result.Loading -> Result.Loading
        is Result.Success -> {
            val share: Share? = shareResult.data
            if (share != null) {
                itemRepository.createAlias(userId, share, newAlias)
            } else {
                Result.Error(IllegalStateException("CreateAlias has invalid share"))
            }
        }
    }
}
