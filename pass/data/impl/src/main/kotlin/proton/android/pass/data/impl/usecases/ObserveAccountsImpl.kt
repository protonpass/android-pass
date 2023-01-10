package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.api.usecases.ObserveAccounts
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import javax.inject.Inject

class ObserveAccountsImpl @Inject constructor(
    private val accountManager: AccountManager
) : ObserveAccounts {

    override fun invoke(): Flow<List<Account>> = accountManager.getAccounts()
}
