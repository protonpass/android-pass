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

package proton.android.pass.crypto.impl

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.GroupKeyDecryptor
import proton.android.pass.log.api.PassLogger

private const val TAG = "CoreGroupKeyDecryptor"

internal class CoreGroupKeyDecryptor(
    private val invitedAddress: UserAddress,
    private val groupPublicKeys: PublicKeyRing,
    private val cryptoContext: CryptoContext
) : GroupKeyDecryptor {

    override fun decryptAndVerify(message: ByteArray): ByteArray {
        val decrypted = invitedAddress.useKeys(cryptoContext) {
            decryptAndVerifyData(
                message = getArmored(message),
                verifyKeyRing = groupPublicKeys
            )
        }
        if (decrypted.status != VerificationStatus.Success) {
            PassLogger.w(TAG, "decryptAndVerify: signature verification failed")
            throw InvalidSignature("ShareKey signature did not match")
        }
        return decrypted.data
    }
}
