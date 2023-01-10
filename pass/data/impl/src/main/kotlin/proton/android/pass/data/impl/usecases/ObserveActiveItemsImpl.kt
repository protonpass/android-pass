package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveActiveShare
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.user.domain.UserManager
import proton.android.pass.common.api.Result
import proton.pass.domain.Item
import proton.pass.domain.ItemState
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveActiveItemsImpl @Inject constructor(
    accountManager: AccountManager,
    private val userManager: UserManager,
    private val observeActiveShare: ObserveActiveShare,
    private val itemRepository: ItemRepository
) : ObserveActiveItems {

    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

    override operator fun invoke(
        filter: ItemTypeFilter
    ): Flow<Result<List<Item>>> = observeActiveShare()
        .flatMapLatest { result: Result<ShareId?> ->
            when (result) {
                is Result.Error -> return@flatMapLatest flowOf(Result.Error(result.exception))
                Result.Loading -> return@flatMapLatest flowOf(Result.Loading)
                is Result.Success -> {
                    flowOf(result.data)
                        .filterNotNull()
                        .combine(getCurrentUserIdFlow.filterNotNull()) { share, user -> share to user }
                        .flatMapLatest { v ->
                            itemRepository.observeItems(
                                userId = v.second.userId,
                                shareSelection = ShareSelection.Share(v.first),
                                itemState = ItemState.Active,
                                itemTypeFilter = filter
                            )
                        }
                }
            }
        }
}

