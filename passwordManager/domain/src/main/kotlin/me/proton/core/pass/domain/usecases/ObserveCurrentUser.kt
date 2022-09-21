package me.proton.core.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import javax.inject.Inject

class ObserveCurrentUser @Inject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager
) {
    operator fun invoke(): Flow<User?> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

}
