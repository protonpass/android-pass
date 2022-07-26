package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.accountmanager.domain.AccountManager

class GetCurrentUserId @Inject constructor(
    private val accountManager: AccountManager
) {
    operator fun invoke() = accountManager.getPrimaryUserId()
}
