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

import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.accesskey.AuthWithAccessKey
import javax.inject.Inject

class AuthWithAccessKeyImpl @Inject constructor(
    private val srpCrypto: SrpCrypto,
    private val encryptionContextProvider: EncryptionContextProvider
) : AuthWithAccessKey {
    override suspend fun invoke(userId: UserId, password: EncryptedString): Boolean {
        val decryptedPassword = encryptionContextProvider.withEncryptionContext {
            decrypt(password).encodeToByteArray()
        }
        srpCrypto.generateSrpProofs(
            username = "", // Not used
            password = decryptedPassword,
            version = VERSION,
            salt = "",
            modulus = "",
            serverEphemeral = ""
        )

        return true
    }

    companion object {
        private const val VERSION = 4L
    }
}
