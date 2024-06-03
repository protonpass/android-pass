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

package proton.android.pass.data.impl.usecases.accesskey

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.accesskey.SetupAccessKey
import proton.android.pass.data.impl.remote.RemoteAccessKeyDataSource
import proton.android.pass.data.impl.repositories.AccessKeyRepository
import proton.android.pass.data.impl.requests.SetupAccessKeyRequest
import javax.inject.Inject

class SetupAccessKeyImpl @Inject constructor(
    private val srpCrypto: SrpCrypto,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val authRepository: AuthRepository,
    private val accountManager: AccountManager,
    private val remoteAccessKeyDataSource: RemoteAccessKeyDataSource,
    private val accessKeyRepository: AccessKeyRepository
) : SetupAccessKey {
    override suspend fun invoke(password: EncryptedString) {
        val decryptedPassword = encryptionContextProvider.withEncryptionContext {
            decrypt(password).encodeToByteArray()
        }

        val account = accountManager.getPrimaryAccount().firstOrNull()
            ?: throw IllegalStateException("No primary account found")
        val modulus = authRepository.randomModulus(account.sessionId)
        val verifier = srpCrypto.calculatePasswordVerifier(
            username = "", // unused
            password = decryptedPassword,
            modulus = modulus.modulus,
            modulusId = modulus.modulusId
        )

        remoteAccessKeyDataSource.setupAccessKey(account.userId, verifier.toRequest())

        accessKeyRepository.storeAccessKeyForUser(account.userId, password)
    }

    private fun Auth.toRequest() = SetupAccessKeyRequest(
        srpParamId = modulusId,
        srpVerifier = verifier,
        srpSalt = salt
    )
}
