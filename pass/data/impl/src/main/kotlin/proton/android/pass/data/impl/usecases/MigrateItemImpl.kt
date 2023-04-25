package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.MigrateItem
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class MigrateItemImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : MigrateItem {

    override suspend fun invoke(
        sourceShare: ShareId,
        itemId: ItemId,
        destinationShare: ShareId
    ): Item {
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        val source = shareRepository.getById(userId, sourceShare)
        val dest = shareRepository.getById(userId, destinationShare)
        return itemRepository.migrateItem(userId, source, dest, itemId)
    }
}
