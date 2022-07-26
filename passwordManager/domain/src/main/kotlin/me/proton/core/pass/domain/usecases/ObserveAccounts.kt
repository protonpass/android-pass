package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId

class ObserveAccounts @Inject constructor(
    private val accountManager: AccountManager,
) {
    operator fun invoke() = accountManager.getAccounts()
}

data class AccountTest(
    val userId: UserId,
    val username: String,
)
