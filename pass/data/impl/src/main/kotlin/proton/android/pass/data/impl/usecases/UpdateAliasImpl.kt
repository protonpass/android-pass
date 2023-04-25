package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import proton.android.pass.data.api.usecases.UpdateAliasItemContent
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.AliasMailbox
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
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
    ): Item {
        if (content.mailboxes is Some) {
            val mailboxes = (content.mailboxes as Some<List<AliasMailbox>>).value
            runCatching {
                aliasRepository.updateAliasMailboxes(
                    userId,
                    item.shareId,
                    item.id,
                    mailboxes
                )
                    .asResultWithoutLoading()
                    .first()
            }.fold(
                onSuccess = {
                    PassLogger.d(TAG, "Alias mailboxes updated")
                },
                onFailure = {
                    PassLogger.d(TAG, it, "Error updating alias mailboxes")
                    throw it
                }
            )
        }

        if (content.itemData is Some) {
            val itemData = (content.itemData as Some<UpdateAliasItemContent>).value
            return updateItemContent(userId, item, itemData)
        }

        return item
    }

    private suspend fun updateItemContent(
        userId: UserId,
        item: Item,
        content: UpdateAliasItemContent
    ): Item {
        val share = getShareById(userId, item.shareId)
        val itemContents = ItemContents.Alias(
            title = content.title,
            note = content.note
        )
        return itemRepository.updateItem(userId, share, item, itemContents)
    }

    companion object {
        private const val TAG = "UpdateAliasImpl"
    }
}

