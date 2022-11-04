package me.proton.pass.domain.usecases

import me.proton.android.pass.log.PassLogger
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.repositories.AliasRepository
import me.proton.pass.domain.repositories.ItemRepository
import javax.inject.Inject

data class UpdateAliasItemContent(
    val title: String,
    val note: String
)

data class UpdateAliasContent(
    val mailboxes: Option<List<AliasMailbox>>,
    val itemData: Option<UpdateAliasItemContent>
)

interface UpdateAlias {
    suspend operator fun invoke(
        userId: UserId,
        item: Item,
        content: UpdateAliasContent
    ): Result<Item>
}

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
            val res = aliasRepository.updateAliasMailboxes(
                userId,
                item.shareId,
                item.id,
                content.mailboxes.value
            )
            if (res is Result.Error) {
                return Result.Error(res.exception)
            }
        }

        if (content.itemData is Some) {
            return updateItemContent(userId, item, content.itemData.value)
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
