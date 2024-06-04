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

package proton.android.pass.data.impl.usecases.extrapassword

import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.extrapassword.AuthWithAccessKey
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordResult
import proton.android.pass.data.impl.remote.RemoteExtraPasswordDataSource
import proton.android.pass.data.impl.requests.ExtraPasswordSendSrpDataRequest
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class AuthWithAccessKeyImpl @Inject constructor(
    private val srpCrypto: SrpCrypto,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val remoteExtraPasswordDataSource: RemoteExtraPasswordDataSource,
    private val sessionManager: SessionManager,
    private val authWithExtraPasswordListener: AuthWithExtraPasswordListenerImpl
) : AuthWithAccessKey {
    override suspend fun invoke(userId: UserId, password: EncryptedString) {
        val decryptedPassword = encryptionContextProvider.withEncryptionContext {
            decrypt(password).encodeToByteArray()
        }

        val srpData = remoteExtraPasswordDataSource.getExtraPasswordAuthData(userId)

        val proofs = srpCrypto.generateSrpProofs(
            username = "", // Not used
            password = decryptedPassword,
            version = srpData.version,
            salt = srpData.srpSalt,
            modulus = srpData.modulus,
            serverEphemeral = srpData.serverEphemeral
        )

        remoteExtraPasswordDataSource.sendExtraPasswordAuthData(
            userId = userId,
            request = ExtraPasswordSendSrpDataRequest(
                clientEphemeral = proofs.clientEphemeral,
                clientProof = proofs.clientProof,
                srpSessionId = srpData.srpSessionId
            )
        )

        PassLogger.i(TAG, "Auth with extra password successful. Refreshing session scopes")
        sessionManager.getSessionId(userId)?.let { session ->
            sessionManager.refreshScopes(session)
        }
        authWithExtraPasswordListener.setState(userId, AuthWithExtraPasswordResult.Success)
    }

    companion object {
        private const val TAG = "AuthWithAccessKeyImpl"
    }
}
