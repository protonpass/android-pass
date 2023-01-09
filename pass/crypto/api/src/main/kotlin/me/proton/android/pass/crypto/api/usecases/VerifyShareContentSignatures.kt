package me.proton.android.pass.crypto.api.usecases

import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.pass.domain.key.VaultKey

data class EncryptedShareResponse(
    val shareId: String,
    val vaultId: String,
    val targetType: Int,
    val targetId: String,
    val permission: Int,
    val acceptanceSignature: String,
    val inviterEmail: String,
    val inviterAcceptanceSignature: String,
    val signingKey: String,
    val signingKeyPassphrase: String?,
    val content: String?,
    val contentFormatVersion: Int?,
    val contentRotationId: String?,
    val contentEncryptedAddressSignature: String?,
    val contentEncryptedVaultSignature: String?,
    val contentSignatureEmail: String?,
    val expirationTime: Long?,
    val createTime: Long
)

interface VerifyShareContentSignatures {
    operator fun invoke(
        response: EncryptedShareResponse,
        contentSignatureKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>
    )
}
