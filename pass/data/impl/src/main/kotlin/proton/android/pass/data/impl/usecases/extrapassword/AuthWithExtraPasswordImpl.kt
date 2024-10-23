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

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.TooManyExtraPasswordAttemptsException
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordResult
import proton.android.pass.data.impl.remote.RemoteExtraPasswordDataSource
import proton.android.pass.data.impl.repositories.ExtraPasswordRepository
import proton.android.pass.data.impl.requests.ExtraPasswordSendSrpDataRequest
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class AuthWithExtraPasswordImpl @Inject constructor(
    private val srpCrypto: SrpCrypto,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val remoteExtraPasswordDataSource: RemoteExtraPasswordDataSource,
    private val sessionManager: SessionManager,
    private val authWithExtraPasswordListener: AuthWithExtraPasswordListenerImpl,
    private val accountManager: AccountManager,
    private val extraPasswordRepository: ExtraPasswordRepository
) : AuthWithExtraPassword {
    override suspend fun invoke(userId: UserId?, password: EncryptedString) {
        val actualUserId = userId ?: accountManager.getPrimaryUserId().first()
            ?: throw IllegalStateException("No user id provided and no primary user found")
        val decryptedPassword = encryptionContextProvider.withEncryptionContext {
            decrypt(password).encodeToByteArray()
        }

        val srpData = remoteExtraPasswordDataSource.getExtraPasswordAuthData(actualUserId)

        val proofs = srpCrypto.generateSrpProofs(
            username = "", // Not used
            password = decryptedPassword,
            version = srpData.version,
            salt = srpData.srpSalt,
            modulus = srpData.modulus,
            serverEphemeral = srpData.serverEphemeral
        )

        runCatching {
            remoteExtraPasswordDataSource.sendExtraPasswordAuthData(
                userId = actualUserId,
                request = ExtraPasswordSendSrpDataRequest(
                    clientEphemeral = proofs.clientEphemeral,
                    clientProof = proofs.clientProof,
                    srpSessionId = srpData.srpSessionId
                )
            )
            extraPasswordRepository.storeAccessKeyForUser(actualUserId, password)

            PassLogger.i(TAG, "Auth with extra password successful. Refreshing session scopes")
            sessionManager.getSessionId(actualUserId)?.let { session ->
                sessionManager.refreshScopes(session)
            }
        }.onSuccess {
            authWithExtraPasswordListener.setState(actualUserId, AuthWithExtraPasswordResult.Success)
        }.onFailure {
            if (it is TooManyExtraPasswordAttemptsException) {
                authWithExtraPasswordListener.setState(actualUserId, AuthWithExtraPasswordResult.Failure)
            }
        }.getOrThrow()
    }

    companion object {
        private const val TAG = "AuthWithAccessKeyImpl"
    }
}
