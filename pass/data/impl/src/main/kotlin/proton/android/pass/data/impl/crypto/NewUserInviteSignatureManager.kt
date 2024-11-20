/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.SignatureContext
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getUnarmored
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.verifyData
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.commonrust.api.NewUserInviteSignatureBodyCreator
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.Constants
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.impl.extensions.tryUseKeys
import proton.android.pass.domain.key.InviteKey
import javax.inject.Inject
import javax.inject.Singleton

interface NewUserInviteSignatureManager {

    fun create(
        inviterUserAddress: UserAddress,
        email: String,
        inviteKey: InviteKey
    ): Result<String>

    fun validate(
        inviterUserAddress: UserAddress,
        signature: String,
        email: String,
        inviteKey: InviteKey
    ): Result<Unit>

}

@Singleton
class NewUserInviteSignatureManagerImpl @Inject constructor(
    private val newUserInviteSignatureBodyCreator: NewUserInviteSignatureBodyCreator,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val context: CryptoContext
) : NewUserInviteSignatureManager {

    override fun create(
        inviterUserAddress: UserAddress,
        email: String,
        inviteKey: InviteKey
    ): Result<String> {
        val signatureBody = encryptionContextProvider.withEncryptionContext {
            val inviteKeyContents = decrypt(inviteKey.key)
            newUserInviteSignatureBodyCreator.create(
                email = email,
                inviteKey = inviteKeyContents
            )
        }

        val signedRawData = inviterUserAddress.tryUseKeys("create new user invite", context) {
            val signature = signData(
                data = signatureBody,
                signatureContext = SignatureContext(
                    value = Constants.SIGNATURE_CONTEXT_NEW_USER,
                    isCritical = true
                )
            )
            getUnarmored(signature)
        }

        val asBase64 = Base64.encodeBase64String(signedRawData)
        return Result.success(asBase64)
    }

    override fun validate(
        inviterUserAddress: UserAddress,
        signature: String,
        email: String,
        inviteKey: InviteKey
    ): Result<Unit> {
        val signatureBody = encryptionContextProvider.withEncryptionContext {
            val inviteKeyContents = decrypt(inviteKey.key)
            newUserInviteSignatureBodyCreator.create(
                email = email,
                inviteKey = inviteKeyContents
            )
        }

        val decodedSignature = Base64.decodeBase64(signature)
        val verified = inviterUserAddress.tryUseKeys("validate user invite", context) {
            val armored = getArmored(
                data = decodedSignature,
                header = PGPHeader.Signature
            )
            verifyData(
                data = signatureBody,
                signature = armored,
                verificationContext = VerificationContext(
                    value = Constants.SIGNATURE_CONTEXT_NEW_USER,
                    required = VerificationContext.ContextRequirement.Required.Always
                )
            )
        }


        return if (verified) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Signature is invalid"))
        }
    }
}
