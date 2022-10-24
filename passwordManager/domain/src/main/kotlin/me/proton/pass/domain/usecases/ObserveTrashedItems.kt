package me.proton.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ShareSelection
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

class ObserveTrashedItems @Inject constructor(
    accountManager: AccountManager,
    private val userManager: UserManager,
    private val observeItems: ObserveItems
) {
    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

    operator fun invoke(): Flow<Result<List<Item>>> = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeItems(user.userId, ShareSelection.AllShares, ItemState.Trashed)
        }
}
