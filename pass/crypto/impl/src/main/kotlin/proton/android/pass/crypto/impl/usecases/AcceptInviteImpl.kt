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

package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.encryptAndSignData
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.getUnarmored
import me.proton.core.key.domain.publicKey
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.User
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.AcceptInvite
import proton.android.pass.crypto.api.usecases.EncryptedInviteKey
import proton.android.pass.crypto.api.usecases.EncryptedInviteShareKeyList
import javax.inject.Inject

class AcceptInviteImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : AcceptInvite {
    override fun invoke(
        invitedUser: User,
        invitedUserAddressKeys: List<PrivateKey>,
        inviterAddressKeys: List<PublicKey>,
        keys: List<EncryptedInviteKey>
    ): EncryptedInviteShareKeyList {

        // KeyHolder that contains the invited user address keys
        val invitedUserPrivateKeyHolder = KeyHolderContext(
            context = cryptoContext,
            privateKeyRing = PrivateKeyRing(
                context = cryptoContext,
                keys = invitedUserAddressKeys
            ),
            publicKeyRing = PublicKeyRing(
                keys = invitedUserAddressKeys.map { it.publicKey(cryptoContext) }
            )
        )

        val inviterUserPublicKeyRing = PublicKeyRing(inviterAddressKeys)

        val reencryptedKeys = invitedUserPrivateKeyHolder.use { keyHolderContext ->
            keys.map { inviteKey ->
                // Invite key comes in Base64. Decode it
                val decodedEncryptedKey = Base64.decodeBase64(inviteKey.key)

                // Invite key is encrypted for the invited user address keys.
                // Decrypt it and verify signature with the inviter address key
                val decryptedKey = keyHolderContext.decryptAndVerifyData(
                    message = cryptoContext.pgpCrypto.getArmored(decodedEncryptedKey),
                    verifyKeyRing = inviterUserPublicKeyRing
                )
                // Check signature verification
                if (decryptedKey.status != VerificationStatus.Success) {
                    throw InvalidSignature("Invite signature did not match")
                }

                // Signature matches. Reencrypt key and sign with user key
                val reencryptedKey = reencryptKey(
                    key = decryptedKey.data,
                    user = invitedUser
                )

                EncryptedInviteKey(
                    keyRotation = inviteKey.keyRotation,
                    key = Base64.encodeBase64String(reencryptedKey)
                )
            }
        }

        return EncryptedInviteShareKeyList(reencryptedKeys)
    }

    private fun reencryptKey(key: ByteArray, user: User): ByteArray = user.useKeys(cryptoContext) {
        val res = encryptAndSignData(
            data = key,
            encryptKeyRing = publicKeyRing,
            signatureContext = null
        )
        getUnarmored(res)
    }
}
