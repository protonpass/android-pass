package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.user.domain.UserManager
import proton.android.pass.common.api.Result
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.pass.domain.Item
import proton.pass.domain.ItemState
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveActiveItemsImpl @Inject constructor(
    accountManager: AccountManager,
    private val userManager: UserManager,
    private val itemRepository: ItemRepository
) : ObserveActiveItems {

    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

    override operator fun invoke(
        filter: ItemTypeFilter,
        shareSelection: ShareSelection
    ): Flow<Result<List<Item>>> = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            itemRepository.observeItems(
                userId = user.userId,
                shareSelection = shareSelection,
                itemState = ItemState.Active,
                itemTypeFilter = filter
            )
        }
}


