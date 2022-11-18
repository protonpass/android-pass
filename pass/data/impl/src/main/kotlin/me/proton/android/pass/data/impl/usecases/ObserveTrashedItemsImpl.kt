package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.android.pass.data.api.usecases.ObserveItems
import me.proton.android.pass.data.api.usecases.ObserveTrashedItems
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.user.domain.UserManager
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ShareSelection
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

    override fun invoke(): Flow<Result<List<Item>>> = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeItems(user.userId, ShareSelection.AllShares, ItemState.Trashed)
        }
}
