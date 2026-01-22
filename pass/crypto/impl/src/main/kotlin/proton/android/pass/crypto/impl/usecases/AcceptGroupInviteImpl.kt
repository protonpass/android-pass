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
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.Constants
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.invites.AcceptGroupInvite
import proton.android.pass.crypto.api.usecases.invites.EncryptedGroupInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteKey
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class AcceptGroupInviteImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val encryptionContextProvider: EncryptionContextProvider
) : AcceptGroupInvite {

    override fun invoke(
        groupPrivateKeys: List<PrivateAddressKey>,
        openerKeys: List<PrivateKey>,
        inviterAddressKeys: List<PublicKey>,
        keys: List<EncryptedInviteKey>
    ): List<EncryptedGroupInviteAcceptKey> {
        val unlockedGroupKeys = unlockGroupKeys(
            groupPrivateKeys = groupPrivateKeys,
            openerPrivateKeys = openerKeys
        )
        return keys.map { inviteKey ->
            processInviteKey(inviteKey, unlockedGroupKeys, inviterAddressKeys)
        }
    }

    private fun processInviteKey(
        inviteKey: EncryptedInviteKey,
        unlockedGroupKeys: List<UnlockedGroupKey>,
        inviterAddressKeys: List<PublicKey>
    ): EncryptedGroupInviteAcceptKey {
        val decoded = Base64.decodeBase64(inviteKey.key)
        val armored = cryptoContext.pgpCrypto.getArmored(decoded)

        val errors = mutableListOf<String>()
        val result = decryptInviteKeyWithGroupKeys(
            armored = armored,
            unlockedGroupKeys = unlockedGroupKeys,
            inviterAddressKeys = inviterAddressKeys,
            errors = errors
        )

        if (result == null) {
            val detailedError = buildInviteKeyErrorMessage(
                keyRotation = inviteKey.keyRotation,
                attemptedKeys = unlockedGroupKeys.size,
                errors = errors
            )
            PassLogger.w(TAG, detailedError)
            error(detailedError)
        }

        val (unlockedGroupKey, data) = result
        return reencryptInviteKey(inviteKey.keyRotation, unlockedGroupKey, data)
    }

    private fun decryptInviteKeyWithGroupKeys(
        armored: String,
        unlockedGroupKeys: List<UnlockedGroupKey>,
        inviterAddressKeys: List<PublicKey>,
        errors: MutableList<String>
    ): Pair<UnlockedGroupKey, ByteArray>? = unlockedGroupKeys.firstNotNullOfOrNull { unlockedGroupKey ->
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
        }.onFailure { error ->
            val errorMsg = "Group key failed: ${error.javaClass.simpleName}: ${error.message}"
            errors.add(errorMsg)
            PassLogger.w(TAG, errorMsg)
        }.getOrNull()
    }

    private fun reencryptInviteKey(
        keyRotation: Long,
        unlockedGroupKey: UnlockedGroupKey,
        data: ByteArray
    ): EncryptedGroupInviteAcceptKey {
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

        return EncryptedGroupInviteAcceptKey(
            keyRotation = keyRotation,
            key = Base64.encodeBase64String(unarmored),
            localEncryptedKey = localEncryptedKey
        )
    }

    private fun buildInviteKeyErrorMessage(
        keyRotation: Long,
        attemptedKeys: Int,
        errors: List<String>
    ): String = buildString {
        append("Invite key (rotation $keyRotation) cannot be decrypted ")
        append("with any of $attemptedKeys group key(s).")
        if (errors.isNotEmpty()) {
            append("\nFailure details:\n")
            errors.forEachIndexed { index, error ->
                append("  ${index + 1}. $error\n")
            }
        }
    }

    private fun unlockGroupKeys(
        groupPrivateKeys: List<PrivateAddressKey>,
        openerPrivateKeys: List<PrivateKey>
    ): List<UnlockedGroupKey> = groupPrivateKeys.map { privateAddressKey ->
        val token = privateAddressKey.token
            ?: error("Missing group address key token for address ${privateAddressKey.addressId}")

        val errors = mutableListOf<String>()
        val decryptedToken = decryptTokenWithOpeners(
            token = token,
            openerKeys = openerPrivateKeys,
            addressId = privateAddressKey.addressId,
            errors = errors
        )

        if (decryptedToken == null) {
            val detailedError = buildDecryptionErrorMessage(
                attemptedKeys = openerPrivateKeys.size,
                errors = errors
            )
            PassLogger.w(TAG, detailedError)
            error(detailedError)
        }

        val unlocked = cryptoContext.pgpCrypto.unlock(
            privateKey = privateAddressKey.privateKey.key,
            passphrase = decryptedToken
        )
        val publicKeyArmored = cryptoContext.pgpCrypto.getPublicKey(privateAddressKey.privateKey.key)
        UnlockedGroupKey(
            publicKey = PublicKey(
                key = publicKeyArmored,
                isPrimary = privateAddressKey.privateKey.isPrimary,
                isActive = privateAddressKey.privateKey.isActive,
                canEncrypt = true,
                canVerify = true
            ),
            unlocked = unlocked
        )
    }

    private fun decryptTokenWithOpeners(
        token: String,
        openerKeys: List<PrivateKey>,
        addressId: String,
        errors: MutableList<String>
    ): ByteArray? {
        PassLogger.d(TAG, "Decrypting token for address=$addressId with ${openerKeys.size} opener key(s)")

        val unlockedOpenerKeys = openerKeys.map { privateKey ->
            val unlockedBytes = unlockOpenerKey(privateKey)
            val armoredUnlocked = cryptoContext.pgpCrypto.getArmored(
                data = unlockedBytes,
                header = me.proton.core.crypto.common.pgp.PGPHeader.PrivateKey
            )
            val publicKeyArmored = cryptoContext.pgpCrypto.getPublicKey(armoredUnlocked)

            UnlockedOpenerKey(
                unlocked = PrivateKey(
                    key = armoredUnlocked,
                    isPrimary = privateKey.isPrimary,
                    isActive = privateKey.isActive,
                    canEncrypt = true,
                    canVerify = true,
                    passphrase = null
                ),
                publicKey = PublicKey(
                    key = publicKeyArmored,
                    isPrimary = privateKey.isPrimary,
                    isActive = privateKey.isActive,
                    canEncrypt = true,
                    canVerify = true
                )
            )
        }

        val keyHolder = KeyHolderContext(
            context = cryptoContext,
            privateKeyRing = PrivateKeyRing(
                context = cryptoContext,
                keys = unlockedOpenerKeys.map { it.unlocked }
            ),
            publicKeyRing = PublicKeyRing(
                keys = unlockedOpenerKeys.map { it.publicKey }
            )
        )

        return keyHolder.use { keyHolderContext ->
            runCatching {
                decryptToken(token, keyHolderContext, addressId)
            }.onFailure { error ->
                val errorMsg = "All keys failed: ${error.javaClass.simpleName}: ${error.message}"
                errors.add(errorMsg)
                PassLogger.w(TAG, errorMsg)
            }.getOrNull()
        }
    }

    private fun decryptToken(
        token: String,
        keyHolder: KeyHolderContext,
        addressId: String
    ): ByteArray {
        PassLogger.d(TAG, "Decrypting token for address=$addressId")
        PassLogger.d(TAG, "  Private keys count: ${keyHolder.privateKeyRing.keys.size}")

        keyHolder.privateKeyRing.keys.forEachIndexed { index, key ->
            runCatching {
                val unlockedBytes = cryptoContext.pgpCrypto.getUnarmored(key.key)
                val decrypted = cryptoContext.pgpCrypto.decryptData(
                    message = token,
                    unlockedKey = unlockedBytes
                )
                PassLogger.d(TAG, "Token decryption SUCCESS with key $index for address=$addressId")
                return decrypted
            }.onFailure { error ->
                PassLogger.w(
                    TAG,
                    "Token decryption failed with key" +
                        " $index: ${error.javaClass.simpleName}: ${error.message}"
                )
            }
        }

        throw IllegalStateException("Could not decrypt token with any key for address=$addressId")
    }

    private fun buildDecryptionErrorMessage(attemptedKeys: Int, errors: List<String>): String = buildString {
        append("Cannot decrypt group key token")
        append("Tried $attemptedKeys opener key(s).")
        if (errors.isNotEmpty()) {
            append("\nFailure details:\n")
            errors.forEachIndexed { index, error ->
                append("  ${index + 1}. $error\n")
            }
        }
    }

    private fun unlockOpenerKey(privateKey: PrivateKey) = privateKey.passphrase?.let { encrypted ->
        val passphrase = cryptoContext.keyStoreCrypto.decrypt(encrypted).array
        cryptoContext.pgpCrypto.unlock(privateKey.key, passphrase).value
    } ?: cryptoContext.pgpCrypto.getUnarmored(privateKey.key)

    data class UnlockedGroupKey(
        val publicKey: PublicKey,
        val unlocked: UnlockedKey
    )

    data class UnlockedOpenerKey(
        val unlocked: PrivateKey,
        val publicKey: PublicKey
    )

    companion object {
        private const val TAG = "GroupInviteCrypto"
    }
}
