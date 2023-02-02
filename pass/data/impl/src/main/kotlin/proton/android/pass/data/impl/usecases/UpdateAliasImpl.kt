package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import proton.android.pass.data.api.usecases.UpdateAliasItemContent
import proton.android.pass.log.api.PassLogger
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResultWithoutLoading
import proton.pass.domain.AliasMailbox
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import javax.inject.Inject

class UpdateAliasImpl @Inject constructor(
    private val aliasRepository: AliasRepository,
    private val itemRepository: ItemRepository,
    private val getShareById: GetShareById
) : UpdateAlias {

    override suspend fun invoke(
        userId: UserId,
        item: Item,
        content: UpdateAliasContent
    ): LoadingResult<Item> {
        if (content.mailboxes is Some) {
            val mailboxes = (content.mailboxes as Some<List<AliasMailbox>>).value
            val res = aliasRepository.updateAliasMailboxes(
                userId,
                item.shareId,
                item.id,
                mailboxes
            )
                .asResultWithoutLoading()
                .first()
            if (res is LoadingResult.Error) {
                return LoadingResult.Error(res.exception)
            }
        }

        if (content.itemData is Some) {
            val itemData = (content.itemData as Some<UpdateAliasItemContent>).value
            return updateItemContent(userId, item, itemData)
        }

        return LoadingResult.Success(item)
    }

    private suspend fun updateItemContent(
        userId: UserId,
        item: Item,
        content: UpdateAliasItemContent
    ): LoadingResult<Item> {
        val share = when (val res = getShare(userId, item.shareId)) {
            is LoadingResult.Success -> res.data
            is LoadingResult.Error -> return LoadingResult.Error(res.exception)
            is LoadingResult.Loading -> return LoadingResult.Loading
        }
        val itemContents = ItemContents.Alias(
            title = content.title,
            note = content.note
        )
        return itemRepository.updateItem(userId, share, item, itemContents)
    }

    private suspend fun getShare(userId: UserId, shareId: ShareId): LoadingResult<Share> =
        when (val res = getShareById(userId, shareId)) {
            is LoadingResult.Success -> {
                val share = res.data
                if (share == null) {
                    val message = "Cannot find share [share_id=$shareId]"
                    PassLogger.i(TAG, message)
                    LoadingResult.Error(IllegalStateException(message))
                } else {
                    LoadingResult.Success(share)
                }
            }
            is LoadingResult.Error -> LoadingResult.Error(res.exception)
            is LoadingResult.Loading -> LoadingResult.Loading
        }

    companion object {
        private const val TAG = "UpdateAliasImpl"
    }
}

