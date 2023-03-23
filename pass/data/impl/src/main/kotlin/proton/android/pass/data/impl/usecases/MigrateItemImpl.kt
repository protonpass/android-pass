package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.MigrateItem
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.Share
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
        val source = getShare(userId, sourceShare)
        val dest = getShare(userId, destinationShare)
        return itemRepository.migrateItem(userId, source, dest, itemId)
    }

    private suspend fun getShare(userId: UserId, shareId: ShareId): Share =
        when (val res = shareRepository.getById(userId, shareId)) {
            LoadingResult.Loading -> throw IllegalStateException("GetShareById cannot return Loading")
            is LoadingResult.Error -> {
                PassLogger.w(TAG, res.exception, "Error getting share by id")
                throw res.exception
            }
            is LoadingResult.Success -> {
                res.data ?: throw ShareNotAvailableError()
            }
        }

    companion object {
        private const val TAG = "MigrateItemImpl"
    }
}
