/*
 * Copyright (c) 2025 Proton AG
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
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.publicKey
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.Constants
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.invites.AcceptGroupInvite
import proton.android.pass.crypto.api.usecases.invites.EncryptedGroupInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteKey
import javax.inject.Inject

class AcceptGroupInviteImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val encryptionContextProvider: EncryptionContextProvider
) : AcceptGroupInvite {

    override fun invoke(
        groupPrivateKeys: List<PrivateAddressKey>,
        organizationPrivateKey: PrivateKey,
        inviterAddressKeys: List<PublicKey>,
        keys: List<EncryptedInviteKey>
    ): List<EncryptedGroupInviteAcceptKey> {
        val unlockedGroupKeys = unlockGroupKeys(
            groupPrivateKeys = groupPrivateKeys,
            organizationPrivateKey = organizationPrivateKey
        )
        return keys.map { inviteKey ->
            val decoded = Base64.decodeBase64(inviteKey.key)
            val armored = cryptoContext.pgpCrypto.getArmored(decoded)

            val (unlockedGroupKey, data) = unlockedGroupKeys.firstNotNullOfOrNull { unlockedGroupKey ->
                runCatching {
                    val decryptedKey = cryptoContext.pgpCrypto.decryptAndVerifyData(
                        message = armored,
                        publicKeys = inviterAddressKeys.map { it.key },
                        unlockedKeys = listOf(unlockedGroupKey.unlocked.value),
                        verificationContext = VerificationContext(
                            value = Constants.SIGNATURE_CONTEXT_EXISTING_USER,
                            required = VerificationContext.ContextRequirement.Required.Always
                        )
                    )
                    if (decryptedKey.status != VerificationStatus.Success) {
                        throw InvalidSignature("Invite signature did not match")
                    }
                    unlockedGroupKey to decryptedKey.data
                }.getOrNull()
            } ?: error("Message cannot be decrypted with any group key")

            val reencryptedKey = cryptoContext.pgpCrypto.encryptAndSignData(
                data = data,
                publicKey = unlockedGroupKey.publicKey.key,
                unlockedKey = unlockedGroupKey.unlocked.value,
                signatureContext = null
            )
            val unarmored = cryptoContext.pgpCrypto.getUnarmored(reencryptedKey)
            val localEncryptedKey = encryptionContextProvider.withEncryptionContext {
                encrypt(data)
            }

            EncryptedGroupInviteAcceptKey(
                keyRotation = inviteKey.keyRotation,
                key = Base64.encodeBase64String(unarmored),
                localEncryptedKey = localEncryptedKey
            )
        }
    }

    private fun unlockGroupKeys(
        groupPrivateKeys: List<PrivateAddressKey>,
        organizationPrivateKey: PrivateKey
    ): List<UnlockedGroupKey> = groupPrivateKeys.map { privateAddressKey ->
        val token = privateAddressKey.token ?: error("Missing group address key token")
        val decryptedToken = cryptoContext.pgpCrypto.decryptData(
            message = token,
            unlockedKey = cryptoContext.pgpCrypto.getUnarmored(organizationPrivateKey.key)
        )
        val unlocked = cryptoContext.pgpCrypto.unlock(
            privateKey = privateAddressKey.privateKey.key,
            passphrase = decryptedToken
        )
        UnlockedGroupKey(
            publicKey = privateAddressKey.privateKey.publicKey(cryptoContext),
            unlocked = unlocked
        )
    }

    data class UnlockedGroupKey(
        val publicKey: PublicKey,
        val unlocked: UnlockedKey
    )
}
