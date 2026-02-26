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
