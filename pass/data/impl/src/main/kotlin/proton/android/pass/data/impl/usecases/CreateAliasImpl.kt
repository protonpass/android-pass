package proton.android.pass.data.impl.usecases

import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.CreateAlias
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Item
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

class CreateAliasImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : CreateAlias {
    override suspend fun invoke(
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
