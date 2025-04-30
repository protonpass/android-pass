package proton.android.pass.account.fakes.accountmanager

import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeAccountWorkflowHandler @Inject constructor() : AccountWorkflowHandler {
    override suspend fun handleSession(account: Account, session: Session) {
        TODO("Not yet implemented")
    }

    override suspend fun handleTwoPassModeNeeded(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleTwoPassModeSuccess(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleTwoPassModeFailed(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleSecondFactorSuccess(sessionId: SessionId, updatedScopes: List<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun handleSecondFactorFailed(sessionId: SessionId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleCreateAddressNeeded(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleCreateAddressSuccess(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleCreateAddressFailed(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleCreateAccountNeeded(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleCreateAccountSuccess(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleCreateAccountFailed(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleDeviceSecretNeeded(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleDeviceSecretSuccess(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleDeviceSecretFailed(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleUnlockFailed(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleAccountReady(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleAccountNotReady(userId: UserId) {
        TODO("Not yet implemented")
    }

    override suspend fun handleAccountDisabled(userId: UserId) {
        TODO("Not yet implemented")
    }
}
