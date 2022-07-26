package me.proton.core.pass.data.crypto

import java.util.Date
import javax.inject.Inject
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyData
import me.proton.core.pass.data.db.entities.ShareEntity
import me.proton.core.pass.data.responses.ShareResponse
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.SharePermission
import me.proton.core.pass.domain.ShareType
import me.proton.core.pass.domain.VaultId
import me.proton.core.pass.domain.key.SigningKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.usePrivateKey
import me.proton.core.user.domain.entity.UserAddress

class OpenShare @Inject constructor(
    val cryptoContext: CryptoContext
) {
    fun open(
        response: ShareResponse,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>
    ): Share {
        val shareType = requireNotNull(ShareType.map[response.targetType])
        verifyAcceptanceSignature(response.acceptanceSignature, response.inviterAcceptanceSignature, response.signingKey, userAddress, inviterKeys)
        val content = reencryptContent(response, vaultKeys)

        return Share(
            id = ShareId(response.shareId),
            shareType = shareType,
            targetId = response.targetId,
            permission = SharePermission(response.permission),
            vaultId = VaultId(response.vaultId),
            signingKey = SigningKey(readSigningKey(response.signingKey, response.signingKeyPassphrase, userAddress)),
            content = content,
            nameKeyId = response.contentRotationId,
            expirationTime = response.expirationTime?.let { Date(it) },
            createTime = Date(response.createTime),
            keys = vaultKeys
        )
    }

    fun responseToEntity(
        response: ShareResponse,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        keystoreEncryptedContent: EncryptedByteArray? = null,
        keystoreEncryptedPassphrase: EncryptedByteArray? = null,
    ): ShareEntity {
        verifyAcceptanceSignature(response.acceptanceSignature, response.inviterAcceptanceSignature, response.signingKey, userAddress, inviterKeys)

        return ShareEntity(
            id = response.shareId,
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            vaultId = response.vaultId,
            targetType = response.targetType,
            targetId = response.targetId,
            permission = response.permission,
            inviterEmail = response.inviterEmail,
            acceptanceSignature = response.acceptanceSignature,
            inviterAcceptanceSignature = response.inviterAcceptanceSignature,
            signingKey = response.signingKey,
            signingKeyPassphrase = response.signingKeyPassphrase,
            content = response.content,
            contentFormatVersion = response.contentFormatVersion,
            contentEncryptedAddressSignature = response.contentEncryptedAddressSignature,
            contentEncryptedVaultSignature = response.contentEncryptedVaultSignature,
            contentSignatureEmail = response.contentSignatureEmail,
            nameKeyId = response.contentRotationId,
            expirationTime = response.expirationTime,
            createTime = response.createTime,

            keystoreEncryptedContent = keystoreEncryptedContent,
            keystoreEncryptedPassphrase = keystoreEncryptedPassphrase,
        )
    }

    fun open(
        entity: ShareEntity,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>
    ): Share {
        val shareType = requireNotNull(ShareType.map[entity.targetType])
        verifyAcceptanceSignature(entity.acceptanceSignature, entity.inviterAcceptanceSignature, entity.signingKey, userAddress, inviterKeys)
        val signingKey = if (entity.keystoreEncryptedPassphrase == null) {
            ArmoredKey.Public(
                entity.signingKey,
                PublicKey(
                    key = entity.signingKey,
                    isPrimary = true,
                    isActive = true,
                    canEncrypt = true,
                    canVerify = true
                )
            )
        } else {
            ArmoredKey.Private(
                entity.signingKey,
                PrivateKey(
                    key = entity.signingKey,
                    isPrimary = true,
                    passphrase = entity.keystoreEncryptedPassphrase
                )
            )
        }

        return Share(
            id = ShareId(entity.id),
            shareType = shareType,
            targetId = entity.targetId,
            permission = SharePermission(entity.permission),
            vaultId = VaultId(entity.vaultId),
            signingKey = SigningKey(signingKey),
            content = entity.keystoreEncryptedContent,
            nameKeyId = entity.nameKeyId,
            expirationTime = entity.expirationTime?.let { Date(it) },
            createTime = Date(entity.createTime),
            keys = vaultKeys
        )
    }

    fun reencryptSigningKeyPassphrase(
        signingKeyPassphrase: String?,
        userAddress: UserAddress
    ): EncryptedByteArray? {
        if (signingKeyPassphrase == null) return null

        return userAddress.useKeys(cryptoContext) {
            val decrypted = decryptData(getArmored(getBase64Decoded(signingKeyPassphrase)))
            PlainByteArray(decrypted).encrypt(cryptoContext.keyStoreCrypto)
        }
    }

    fun reencryptContent(response: ShareResponse, vaultKeys: List<VaultKey>): EncryptedByteArray? {
        if (response.content == null || response.contentRotationId == null) return null

        val key = vaultKeys.first { it.rotationId == response.contentRotationId }
        return key.usePrivateKey(cryptoContext) {
            val decrypted = decryptData(getArmored(getBase64Decoded(response.content)))
            PlainByteArray(decrypted).encrypt(cryptoContext.keyStoreCrypto)
        }
    }

    private fun verifyAcceptanceSignature(
        acceptanceSignature: String,
        inviterAcceptanceSignature: String,
        signingKey: Armored,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>
    ) {
        userAddress.useKeys(cryptoContext) {
            // Check Signing Key Signature
            val signingKeyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, signingKey)
            val armoredAcceptanceSignature = getArmored(getBase64Decoded(acceptanceSignature), PGPHeader.Signature)
            val verified = verifyData(signingKeyFingerprint.encodeToByteArray(), armoredAcceptanceSignature)
            require(verified)

            // Check inviter acceptance signature
            val publicKeyRing = PublicKeyRing(inviterKeys)
            val armoredInviterAcceptanceSignature = getArmored(getBase64Decoded(inviterAcceptanceSignature), PGPHeader.Signature)
            val inviterVerified = publicKeyRing.verifyData(cryptoContext, signingKeyFingerprint.encodeToByteArray(), armoredInviterAcceptanceSignature)
            require(inviterVerified)
        }
    }

    private fun readSigningKey(
        signingKey: Armored,
        signingKeyPassphrase: String?,
        userAddress: UserAddress,
    ): ArmoredKey {
        if (signingKeyPassphrase == null) {
            return ArmoredKey.Public(
                signingKey,
                PublicKey(
                    key = signingKey,
                    isPrimary = true,
                    isActive = true,
                    canEncrypt = true,
                    canVerify = true
                )
            )
        }

        val signingKeyPrivateKey = PrivateKey(
            key = signingKey, isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = reencryptSigningKeyPassphrase(signingKeyPassphrase, userAddress)
        )

        return ArmoredKey.Private(signingKey, signingKeyPrivateKey)
    }
}
