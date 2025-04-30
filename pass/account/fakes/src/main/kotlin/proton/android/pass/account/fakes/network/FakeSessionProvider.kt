package proton.android.pass.account.fakes.network

import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("NotImplementedDeclaration")
class FakeSessionProvider @Inject constructor() : SessionProvider {
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
