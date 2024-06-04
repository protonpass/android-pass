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

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.extrapassword.SetupExtraPassword
import proton.android.pass.data.impl.remote.RemoteExtraPasswordDataSource
import proton.android.pass.data.impl.repositories.ExtraPasswordRepository
import proton.android.pass.data.impl.requests.SetupExtraPasswordRequest
import javax.inject.Inject

class SetupExtraPasswordImpl @Inject constructor(
    private val srpCrypto: SrpCrypto,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val authRepository: AuthRepository,
    private val accountManager: AccountManager,
    private val remoteExtraPasswordDataSource: RemoteExtraPasswordDataSource,
    private val extraPasswordRepository: ExtraPasswordRepository
) : SetupExtraPassword {
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

        remoteExtraPasswordDataSource.setupExtraPassword(account.userId, verifier.toRequest())

        extraPasswordRepository.storeAccessKeyForUser(account.userId, password)
    }

    private fun Auth.toRequest() = SetupExtraPasswordRequest(
        modulusId = modulusId,
        srpVerifier = verifier,
        srpSalt = salt
    )
}
