/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.account.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAccountManager @Inject constructor() : AccountManager(Product.Pass) {

    private val primaryUserIdFlow: MutableStateFlow<UserId?> =
        MutableStateFlow(UserId(USER_ID))

    private val accountFlow: MutableStateFlow<Account> = MutableStateFlow(DEFAULT_ACCOUNT)
    private val accountsFlow = MutableStateFlow(listOf(DEFAULT_ACCOUNT))

    override suspend fun addAccount(account: Account, session: Session) {
        // no-op
    }

    override fun getAccount(userId: UserId): Flow<Account?> = accountFlow

    override fun getAccounts(): Flow<List<Account>> = accountsFlow

    fun setAccounts(accounts: List<Account>) {
        accountsFlow.update { accounts }
    }

    override suspend fun getPreviousPrimaryUserId(): UserId? = null

    override fun getPrimaryUserId(): Flow<UserId?> = primaryUserIdFlow

    fun sendPrimaryUserId(userId: UserId?) = primaryUserIdFlow.tryEmit(userId)

    override fun getSessions(): Flow<List<Session>> = emptyFlow()

    override fun onAccountStateChanged(initialState: Boolean): Flow<Account> = emptyFlow()

    override fun onSessionStateChanged(initialState: Boolean): Flow<Account> = emptyFlow()

    override suspend fun removeAccount(userId: UserId, waitForCompletion: Boolean) {
        // no-op
    }

    override suspend fun disableAccount(
        userId: UserId,
        waitForCompletion: Boolean,
        keepSession: Boolean
    ) {
        // no-op
    }

    override suspend fun setAsPrimary(userId: UserId) {
        // no-op
    }

    companion object {
        const val USER_ID = "FakeAccountManager-DefaultUserId"
        val DEFAULT_ACCOUNT = createAccount(UserId(USER_ID))

        fun createAccount(userId: UserId) = Account(
            userId = userId,
            state = AccountState.Ready,
            details = AccountDetails(null, null),
            username = null,
            email = null,
            sessionId = null,
            sessionState = null
        )
    }
}
