package me.proton.android.pass.data.impl.crypto

import me.proton.android.pass.crypto.api.usecases.VerifyAcceptanceSignature
import me.proton.android.pass.crypto.api.usecases.VerifyShareContentSignatures
import me.proton.android.pass.data.impl.db.entities.ShareEntity
import me.proton.android.pass.data.impl.extensions.toCrypto
import me.proton.android.pass.data.impl.responses.ShareResponse
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.domain.key.VaultKey
import javax.inject.Inject

interface ShareResponseToEntity {
    operator fun invoke(
        response: ShareResponse,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        contentSignatureKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        keystoreEncryptedContent: EncryptedByteArray? = null,
        keystoreEncryptedPassphrase: EncryptedByteArray? = null
    ): ShareEntity
}

class ShareResponseToEntityImpl @Inject constructor(
    private val verifyAcceptanceSignature: VerifyAcceptanceSignature,
    private val verifyShareContentSignatures: VerifyShareContentSignatures
) : ShareResponseToEntity {

    override fun invoke(
        response: ShareResponse,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        contentSignatureKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        keystoreEncryptedContent: EncryptedByteArray?,
        keystoreEncryptedPassphrase: EncryptedByteArray?
    ): ShareEntity {
        verifyAcceptanceSignature(
            response.acceptanceSignature,
            response.inviterAcceptanceSignature,
            response.signingKey,
            userAddress,
            inviterKeys
        )
        verifyShareContentSignatures(response.toCrypto(), contentSignatureKeys, vaultKeys)

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
}
