package me.proton.android.pass.data.impl.crypto

import me.proton.android.pass.data.impl.db.entities.ShareEntity
import me.proton.android.pass.data.impl.error.InvalidAddressSignature
import me.proton.android.pass.data.impl.error.InvalidSignature
import me.proton.android.pass.data.impl.error.KeyNotFound
import me.proton.android.pass.data.impl.responses.ShareResponse
import me.proton.android.pass.log.PassLogger
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
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.SharePermission
import me.proton.pass.domain.ShareType
import me.proton.pass.domain.VaultId
import me.proton.pass.domain.key.SigningKey
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.domain.key.publicKey
import me.proton.pass.domain.key.usePrivateKey
import java.sql.Date
import javax.inject.Inject

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
        verifyAcceptanceSignature(
            response.acceptanceSignature,
            response.inviterAcceptanceSignature,
            response.signingKey,
            userAddress,
            inviterKeys
        )
        val content = reencryptContent(response, vaultKeys)

        return Share(
            id = ShareId(response.shareId),
            shareType = shareType,
            targetId = response.targetId,
            permission = SharePermission(response.permission),
            vaultId = VaultId(response.vaultId),
            signingKey = SigningKey(
                readSigningKey(
                    response.signingKey,
                    response.signingKeyPassphrase,
                    userAddress
                )
            ),
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
        contentSignatureKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        keystoreEncryptedContent: EncryptedByteArray? = null,
        keystoreEncryptedPassphrase: EncryptedByteArray? = null
    ): ShareEntity {
        verifyAcceptanceSignature(
            response.acceptanceSignature,
            response.inviterAcceptanceSignature,
            response.signingKey,
            userAddress,
            inviterKeys
        )
        verifyContentSignatures(response, contentSignatureKeys, vaultKeys)

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
            keystoreEncryptedPassphrase = keystoreEncryptedPassphrase
        )
    }

    fun open(
        entity: ShareEntity,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>
    ): Share {
        val shareType = ShareType.map[entity.targetType]
        if (shareType == null) {
            val e = CryptoException("Unknown ShareType")
            PassLogger.e(TAG, e, "Unknown ShareType [shareType=${entity.targetType}]")
            throw e
        }

        verifyAcceptanceSignature(
            entity.acceptanceSignature,
            entity.inviterAcceptanceSignature,
            entity.signingKey,
            userAddress,
            inviterKeys
        )
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

    @Suppress("ThrowsCount")
    private fun verifyContentSignatures(
        response: ShareResponse,
        contentSignatureKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>
    ) {
        // Check share contents
        if (response.targetType == ShareType.Item.value) {
            return
        }

        val contentRotationId = response.contentRotationId
            ?: throw CryptoException("Share should contain contentRotationId")
        val contentEncryptedAddressSignature = response.contentEncryptedAddressSignature
            ?: throw CryptoException("Share should contain contentEncryptedAddressSignature")
        val contentEncryptedVaultSignature = response.contentEncryptedVaultSignature
            ?: throw CryptoException("Share should contain contentEncryptedVaultSignature")
        val content = response.content ?: throw CryptoException("Share should contain content")

        // Obtain the vault key
        val vaultKey = vaultKeys.find {
            it.rotationId == contentRotationId
        }

        if (vaultKey == null) {
            val e = KeyNotFound("VaultKey not found")
            PassLogger.e(
                TAG,
                e,
                "VaultKey not found when opening share [shareId=${response.shareId}]" +
                    "[vaultKey.contentRotationId=${response.contentRotationId}]"
            )
            throw e
        }

        // Decrypt the signatures
        val decryptWithVaultKey = { data: String ->
            vaultKey.usePrivateKey(cryptoContext) {
                decryptData(getArmored(getBase64Decoded(data)))
            }
        }

        val addressSignature = decryptWithVaultKey(contentEncryptedAddressSignature)
        val vaultSignature = decryptWithVaultKey(contentEncryptedVaultSignature)
        val decryptedContent = decryptWithVaultKey(content)

        // Verify address signature
        val armoredAddressSignature =
            cryptoContext.pgpCrypto.getArmored(addressSignature, PGPHeader.Signature)
        val addressSignatureValid = contentSignatureKeys.any {
            cryptoContext.pgpCrypto.verifyData(decryptedContent, armoredAddressSignature, it.key)
        }

        if (!addressSignatureValid) {
            val e = InvalidAddressSignature()
            PassLogger.e(TAG, e, "Address signature not valid [shareId=${response.shareId}]")
            throw e
        }


        // Verify vault signature
        val vaultSignatureValid = cryptoContext.pgpCrypto.verifyData(
            data = decryptedContent,
            signature = cryptoContext.pgpCrypto.getArmored(vaultSignature, PGPHeader.Signature),
            publicKey = vaultKey.publicKey(cryptoContext).key
        )
        if (!vaultSignatureValid) {
            val e = InvalidSignature("Vault signature is not valid")
            PassLogger.e(TAG, e, "Vault signature is not valid [shareId=${response.shareId}]")
            throw e
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
            val armoredAcceptanceSignature = getArmored(
                getBase64Decoded(acceptanceSignature),
                PGPHeader.Signature
            )
            val verified =
                verifyData(signingKeyFingerprint.encodeToByteArray(), armoredAcceptanceSignature)
            if (!verified) {
                val e = InvalidSignature("Acceptance signature")
                PassLogger.e(TAG, e, "Acceptance signature failed to verify")
                throw e
            }

            // Check inviter acceptance signature
            val publicKeyRing = PublicKeyRing(inviterKeys)
            val armoredInviterAcceptanceSignature =
                getArmored(getBase64Decoded(inviterAcceptanceSignature), PGPHeader.Signature)
            val inviterVerified = publicKeyRing.verifyData(
                cryptoContext,
                signingKeyFingerprint.encodeToByteArray(),
                armoredInviterAcceptanceSignature
            )
            if (!inviterVerified) {
                val e = InvalidAddressSignature()
                PassLogger.e(TAG, e, "Share inviterAcceptanceSignature failed to verify")
                throw e
            }
        }
    }

    private fun readSigningKey(
        signingKey: Armored,
        signingKeyPassphrase: String?,
        userAddress: UserAddress
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

    companion object {
        private const val TAG = "OpenShare"
    }
}
