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

package proton.android.pass.data.impl.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.getArmored
import me.proton.core.user.domain.entity.User
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.GetUserWithKeys
import proton.android.pass.data.impl.extensions.tryUseKeys
import proton.android.pass.domain.UserId
import javax.inject.Inject

interface GetUserKeys {
    suspend operator fun invoke(userId: UserId): UserKeys
}

class GetUserKeysImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val getUserWithKeys: GetUserWithKeys
) : GetUserKeys {

    override suspend fun invoke(userId: UserId): UserKeys = CoreUserKeys(
        user = getUserWithKeys(userId),
        cryptoContext = cryptoContext
    )
}

internal class CoreUserKeys(
    private val user: User,
    private val cryptoContext: CryptoContext
) : UserKeys {

    override fun hasActiveKey(keyId: String): Boolean = user.keys.any {
        it.active == true && keyId == it.keyId.id
    }

    override fun decryptAndVerify(message: ByteArray): ByteArray {
        val decrypted = user.tryUseKeys("reencrypt share key", cryptoContext) {
            decryptAndVerifyData(getArmored(message))
        }

        if (decrypted.status != VerificationStatus.Success) {
            throw InvalidSignature("ShareKey signature did not match")
        }

        return decrypted.data
    }
}
