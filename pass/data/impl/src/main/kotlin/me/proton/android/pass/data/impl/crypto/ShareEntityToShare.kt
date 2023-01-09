package me.proton.android.pass.data.impl.crypto

import me.proton.android.pass.crypto.api.error.CryptoException
import me.proton.android.pass.crypto.api.usecases.ReadKey
import me.proton.android.pass.crypto.api.usecases.VerifyAcceptanceSignature
import me.proton.android.pass.data.api.repositories.VaultKeyRepository
import me.proton.android.pass.data.impl.db.entities.ShareEntity
import me.proton.android.pass.log.api.PassLogger
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.SharePermission
import me.proton.pass.domain.ShareType
import me.proton.pass.domain.VaultId
import me.proton.pass.domain.key.SigningKey
import me.proton.pass.domain.key.VaultKey
import java.sql.Date
import javax.inject.Inject

interface ShareEntityToShare {
    suspend operator fun invoke(
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        entity: ShareEntity
    ): Result<Share>
}

class ShareEntityToShareImpl @Inject constructor(
    private val vaultKeyRepository: VaultKeyRepository,
    private val verifyAcceptanceSignature: VerifyAcceptanceSignature,
    private val readKey: ReadKey
) : ShareEntityToShare {

    override suspend fun invoke(
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        entity: ShareEntity
    ): Result<Share> {
        val signingKey = SigningKey(readKey(entity.signingKey, isPrimary = true))
        return vaultKeyRepository.getVaultKeys(userAddress, ShareId(entity.id), signingKey)
            .map { vaultKeys ->
                try {
                    convert(entity, userAddress, inviterKeys, vaultKeys)
                } catch (e: IllegalArgumentException) {
                    return Result.Error(e)
                }
            }
    }

    private fun convert(
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
            acceptanceSignature = entity.acceptanceSignature,
            inviterAcceptanceSignature = entity.inviterAcceptanceSignature,
            signingKey = entity.signingKey,
            userAddress = userAddress,
            inviterKeys = inviterKeys
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

    companion object {
        private const val TAG = "ShareEntityToShareImpl"
    }

}
