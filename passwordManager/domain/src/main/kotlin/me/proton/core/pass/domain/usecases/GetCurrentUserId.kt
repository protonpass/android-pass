package me.proton.core.pass.domain.usecases

import me.proton.core.accountmanager.domain.AccountManager
import javax.inject.Inject

class GetCurrentUserId @Inject constructor(
    private val accountManager: AccountManager
) {
    operator fun invoke() = accountManager.getPrimaryUserId()
}
