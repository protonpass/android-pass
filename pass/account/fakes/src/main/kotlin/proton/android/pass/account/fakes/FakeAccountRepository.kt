/*
 * Copyright (c) 2024 Proton AG
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
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class FakeAccountRepository @Inject constructor() : AccountRepository {
    override suspend fun addMigration(userId: UserId, migration: String) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun clearSessionDetails(sessionId: SessionId) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun createOrUpdateAccountSession(account: Account, session: Session) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun createOrUpdateSession(userId: UserId?, session: Session) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun deleteAccount(userId: UserId) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun deleteSession(sessionId: SessionId) {
        throw IllegalStateException("Not implemented")
    }

    override fun getAccount(userId: UserId): Flow<Account?> {
        throw IllegalStateException("Not implemented")
    }

    override fun getAccount(sessionId: SessionId): Flow<Account?> {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getAccountOrNull(userId: UserId): Account? {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getAccountOrNull(sessionId: SessionId): Account? {
        throw IllegalStateException("Not implemented")
    }

    override fun getAccounts(): Flow<List<Account>> {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getPreviousPrimaryUserId(): UserId? {
        throw IllegalStateException("Not implemented")
    }

    override fun getPrimaryUserId(): Flow<UserId?> {
        throw IllegalStateException("Not implemented")
    }

    override fun getSession(sessionId: SessionId): Flow<Session?> {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getSessionDetails(sessionId: SessionId): SessionDetails? {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getSessionIdOrNull(userId: UserId?): SessionId? {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getSessionOrNull(sessionId: SessionId): Session? {
        throw IllegalStateException("Not implemented")
    }

    override fun getSessions(): Flow<List<Session>> {
        throw IllegalStateException("Not implemented")
    }

    override fun onAccountStateChanged(initialState: Boolean): Flow<Account> {
        throw IllegalStateException("Not implemented")
    }

    override fun onSessionStateChanged(initialState: Boolean): Flow<Account> {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun removeMigration(userId: UserId, migration: String) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun setAsPrimary(userId: UserId) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun setSessionDetails(sessionId: SessionId, details: SessionDetails) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateAccountState(userId: UserId, state: AccountState) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateAccountState(sessionId: SessionId, state: AccountState) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateSessionScopes(sessionId: SessionId, scopes: List<String>) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateSessionState(userId: UserId, state: SessionState) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateSessionState(sessionId: SessionId, state: SessionState) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateSessionToken(
        sessionId: SessionId,
        accessToken: String,
        refreshToken: String
    ) {
        throw IllegalStateException("Not implemented")
    }
}
