package me.proton.pass.domain.usecases

import me.proton.core.accountmanager.domain.AccountManager
import javax.inject.Inject

class ObserveAccounts @Inject constructor(
    private val accountManager: AccountManager
) {
    operator fun invoke() = accountManager.getAccounts()
}
