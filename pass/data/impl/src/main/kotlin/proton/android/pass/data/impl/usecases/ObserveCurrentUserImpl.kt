package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import javax.inject.Inject

class ObserveCurrentUserImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager
) : ObserveCurrentUser {

    override fun invoke(): Flow<User> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .filterNotNull()
        .distinctUntilChanged()
}

