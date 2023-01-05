package me.proton.pass.test

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onStart
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import javax.inject.Inject

class TestAccountManager @Inject constructor() : AccountManager(Product.Drive) {

    private val primaryUserIdFlow: MutableSharedFlow<UserId?> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override suspend fun addAccount(account: Account, session: Session) {
        // no-op
    }

    override suspend fun disableAccount(userId: UserId) {
        // no-op
    }

    override fun getAccount(userId: UserId): Flow<Account?> = emptyFlow()

    override fun getAccounts(): Flow<List<Account>> = emptyFlow()

    override suspend fun getPreviousPrimaryUserId(): UserId? = null

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
