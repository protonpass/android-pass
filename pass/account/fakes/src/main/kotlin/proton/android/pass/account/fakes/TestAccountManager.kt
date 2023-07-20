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

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onStart
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestAccountManager @Inject constructor() : AccountManager(Product.Pass) {

    private val primaryUserIdFlow: MutableStateFlow<UserId?> =
        MutableStateFlow(UserId("TestAccountManager-DefaultUserId"))

    override suspend fun addAccount(account: Account, session: Session) {
        // no-op
    }

    override suspend fun disableAccount(userId: UserId) {
        // no-op
    }

    override fun getAccount(userId: UserId): Flow<Account?> = emptyFlow()

    override fun getAccounts(): Flow<List<Account>> = emptyFlow()

    override suspend fun getPreviousPrimaryUserId(): UserId? = null

    @Suppress("MagicNumber")
    override fun getPrimaryUserId(): Flow<UserId?> = primaryUserIdFlow
        .onStart { delay(500) }

    fun sendPrimaryUserId(userId: UserId?) = primaryUserIdFlow.tryEmit(userId)

    override fun getSessions(): Flow<List<Session>> = emptyFlow()

    override fun onAccountStateChanged(initialState: Boolean): Flow<Account> = emptyFlow()

    override fun onSessionStateChanged(initialState: Boolean): Flow<Account> = emptyFlow()

    override suspend fun removeAccount(userId: UserId) {
        // no-op
    }

    override suspend fun setAsPrimary(userId: UserId) {
        // no-op
    }
}
