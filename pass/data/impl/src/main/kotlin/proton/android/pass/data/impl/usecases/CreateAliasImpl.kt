package proton.android.pass.data.impl.usecases

import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.CreateAlias
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
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
    ): LoadingResult<Item> = when (val shareResult = shareRepository.getById(userId, shareId)) {
        is LoadingResult.Error -> LoadingResult.Error(shareResult.exception)
        LoadingResult.Loading -> LoadingResult.Loading
        is LoadingResult.Success -> {
            val share: Share? = shareResult.data
            if (share != null) {
                itemRepository.createAlias(userId, share, newAlias)
            } else {
                LoadingResult.Error(IllegalStateException("CreateAlias has invalid share"))
            }
        }
    }
}
