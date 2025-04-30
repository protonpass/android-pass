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
