package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.android.pass.data.api.repositories.AliasRepository
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.usecases.GetShareById
import me.proton.android.pass.data.api.usecases.UpdateAlias
import me.proton.android.pass.data.api.usecases.UpdateAliasContent
import me.proton.android.pass.data.api.usecases.UpdateAliasItemContent
import me.proton.android.pass.log.PassLogger
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
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
    ): Result<Item> {
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
            if (res is Result.Error) {
                return Result.Error(res.exception)
            }
        }

        if (content.itemData is Some) {
            val itemData = (content.itemData as Some<UpdateAliasItemContent>).value
            return updateItemContent(userId, item, itemData)
        }

        return Result.Success(item)
    }

    private suspend fun updateItemContent(
        userId: UserId,
        item: Item,
        content: UpdateAliasItemContent
    ): Result<Item> {
        val share = when (val res = getShare(userId, item.shareId)) {
            is Result.Success -> res.data
            is Result.Error -> return Result.Error(res.exception)
            is Result.Loading -> return Result.Loading
        }
        val itemContents = ItemContents.Alias(
            title = content.title,
            note = content.note
        )
        return itemRepository.updateItem(userId, share, item, itemContents)
    }

    private suspend fun getShare(userId: UserId, shareId: ShareId): Result<Share> =
        when (val res = getShareById(userId, shareId)) {
            is Result.Success -> {
                val share = res.data
                if (share == null) {
                    val message = "Cannot find share [share_id=$shareId]"
                    PassLogger.i(TAG, message)
                    Result.Error(IllegalStateException(message))
                } else {
                    Result.Success(share)
                }
            }
            is Result.Error -> Result.Error(res.exception)
            is Result.Loading -> Result.Loading
        }

    companion object {
        private const val TAG = "UpdateAliasImpl"
    }
}

