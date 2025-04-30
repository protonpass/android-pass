package proton.android.pass.account.fakes.auth

import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.entity.RawSessionForkPayload
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SessionForkSelector
import me.proton.core.auth.domain.entity.SessionForkUserCode
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("NotImplementedDeclaration", "TooManyFunctions")
class FakeAuthRepository @Inject constructor() : AuthRepository {
    override suspend fun getAuthInfoAuto(sessionId: SessionId?, username: String): AuthInfo {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthInfoSrp(sessionId: SessionId?, username: String): AuthInfo.Srp {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthInfoSso(sessionId: SessionId?, email: String): AuthInfo.Sso {
        TODO("Not yet implemented")
    }

    override suspend fun performLogin(
        username: String,
        srpProofs: SrpProofs,
        srpSession: String,
        frames: List<ChallengeFrameDetails>
    ): SessionInfo {
        TODO("Not yet implemented")
    }

    override suspend fun performLoginSso(email: String, token: String): SessionInfo {
        TODO("Not yet implemented")
    }

    override suspend fun performLoginLess(frames: List<ChallengeFrameDetails>): SessionInfo {
        TODO("Not yet implemented")
    }

    override suspend fun performSecondFactor(sessionId: SessionId, secondFactorProof: SecondFactorProof): ScopeInfo {
        TODO("Not yet implemented")
    }

    override suspend fun revokeSession(sessionId: SessionId, revokeAuthDevice: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun randomModulus(sessionId: SessionId?): Modulus {
        TODO("Not yet implemented")
    }

    override suspend fun getScopes(sessionId: SessionId?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun requestSession(): ApiResult<Session> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshSession(session: Session): ApiResult<Session> {
        TODO("Not yet implemented")
    }

    override suspend fun validateEmail(email: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun validatePhone(phone: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun forkSession(
        sessionId: SessionId,
        payload: String?,
        childClientId: String,
        independent: Long,
        userCode: String?
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun getSessionForks(sessionId: SessionId?): Pair<SessionForkSelector, SessionForkUserCode> {
        TODO("Not yet implemented")
    }

    override suspend fun getForkedSession(
        selector: SessionForkSelector
    ): Pair<RawSessionForkPayload?, Session.Authenticated> {
        TODO("Not yet implemented")
    }
}
