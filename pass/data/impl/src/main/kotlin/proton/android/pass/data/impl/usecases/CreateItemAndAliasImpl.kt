package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.CreateItemAndAlias
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

class CreateItemAndAliasImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository
) : CreateItemAndAlias {

    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        itemContents: ItemContents,
        newAlias: NewAlias
    ): Item {
        val id = userId ?: requireNotNull(accountManager.getPrimaryUserId().first())
        return createItemAndAlias(id, shareId, itemContents, newAlias)
    }

    private suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents,
        newAlias: NewAlias
    ): Item = itemRepository.createItemAndAlias(userId, shareId, itemContents, newAlias)

}
