/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.account.fakes.accountmanager

import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeSessionManager @Inject constructor() : SessionManager {
    override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T {
        TODO("Not yet implemented")
    }

    override suspend fun requestSession(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun refreshSession(session: Session): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun refreshScopes(sessionId: SessionId) {
        TODO("Not yet implemented")
    }

    override suspend fun getSession(sessionId: SessionId?): Session? {
        TODO("Not yet implemented")
    }

    override suspend fun getSessions(): List<Session> {
        TODO("Not yet implemented")
    }

    override suspend fun getSessionId(userId: UserId?): SessionId? {
        TODO("Not yet implemented")
    }

    override suspend fun getUserId(sessionId: SessionId): UserId? {
        TODO("Not yet implemented")
    }
}
