package me.proton.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

interface ObserveActiveShare {
    operator fun invoke(): Flow<Result<ShareId?>>
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

    override operator fun invoke(): Flow<Result<ShareId?>> = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeShares(user.userId)
                .map { sharesResult: Result<List<Share>> ->
                    when (sharesResult) {
                        is Result.Error -> return@map Result.Error(sharesResult.exception)
                        Result.Loading -> return@map Result.Loading
                        is Result.Success -> Unit
                    }
                    Result.Success(sharesResult.data.firstOrNull()?.id)
                }
        }
        .distinctUntilChanged()

}
