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
import me.proton.core.key.domain.encryptAndSignData
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.publicKey
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.EncryptInviteKeys
import proton.android.pass.crypto.api.usecases.EncryptedInviteKey
import proton.android.pass.crypto.api.usecases.EncryptedInviteShareKeyList
import proton.pass.domain.key.ShareKey
import javax.inject.Inject

class EncryptInviteKeysImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val encryptionContextProvider: EncryptionContextProvider
) : EncryptInviteKeys {

    override fun invoke(
        inviterAddressKey: PrivateKey,
        shareKeys: List<ShareKey>,
        targetAddressKey: PublicKey
    ): EncryptedInviteShareKeyList {
        // Set up targetAddressKey
        val targetAddressPublicKeyRing = PublicKeyRing(listOf(targetAddressKey))

        // Set up inviterAddressKey
        val inviterAddressPrivateKeyRing = PrivateKeyRing(cryptoContext, listOf(inviterAddressKey))
        val inviterAddressPublicKeyRing = PublicKeyRing(listOf(inviterAddressKey.publicKey(cryptoContext)))
        val inviterKeyHolder = KeyHolderContext(
            context = cryptoContext,
            privateKeyRing = inviterAddressPrivateKeyRing,
            publicKeyRing = inviterAddressPublicKeyRing
        )

        // Decrypt share keys so we have the actual value
        val decryptedShareKeys: List<EncryptionKeyWithRotation> = encryptionContextProvider.withEncryptionContext {
            shareKeys.map {
                EncryptionKeyWithRotation(
                    key = decrypt(it.key),
                    rotation = it.rotation
                )
            }
        }

        // Encrypt share keys for the target address, signing it with the inviter address key
        val reencryptedShareKeys: List<EncryptedInviteKey> = inviterKeyHolder.use { keyHolder ->
            decryptedShareKeys.map { decryptedKey ->
                val armoredEncrypted = keyHolder.encryptAndSignData(
                    data = decryptedKey.key,
                    encryptKeyRing = targetAddressPublicKeyRing,
                )
                val unarmoredEncrypted = cryptoContext.pgpCrypto.getUnarmored(armoredEncrypted)
                EncryptedInviteKey(
                    keyRotation = decryptedKey.rotation,
                    key = Base64.encodeBase64String(unarmoredEncrypted)
                )
            }
        }

        return EncryptedInviteShareKeyList(reencryptedShareKeys)
    }
}

private data class EncryptionKeyWithRotation(
    val key: ByteArray,
    val rotation: Long
)
