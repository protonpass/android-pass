package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveTrashedItems
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.user.domain.UserManager
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Item
import proton.pass.domain.ItemState
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveTrashedItemsImpl @Inject constructor(
    accountManager: AccountManager,
    private val userManager: UserManager,
    private val observeItems: ObserveItems
) : ObserveTrashedItems {
    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

    override fun invoke(): Flow<LoadingResult<List<Item>>> = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeItems(user.userId, ShareSelection.AllShares, ItemState.Trashed)
        }
}
