package me.proton.core.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.domain.ShareId
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

interface ObserveActiveShare {
    operator fun invoke(): Flow<ShareId?>
}

class ObserveActiveShareImpl @Inject constructor(
    accountManager: AccountManager,
    private val userManager: UserManager,
    private val observeShares: ObserveShares
) : ObserveActiveShare {

    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

    override operator fun invoke(): Flow<ShareId?> = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeShares(user.userId).map { shares ->
                shares.firstOrNull()?.id
            }
        }
        .distinctUntilChanged()

}
