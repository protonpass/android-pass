package me.proton.core.pass.data.crypto

import javax.inject.Inject
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.getEncryptedPackets
import me.proton.core.key.domain.publicKey
import me.proton.core.pass.data.responses.ShareResponse
import proton_key_vault_v1.VaultV1

class OpenShareContents @Inject constructor(
    val cryptoContext: CryptoContext
) {
    fun openVaultShareContents(shareResponse: ShareResponse, vaultKey: PrivateKey): VaultV1.Vault {
        val keyHolderContext = KeyHolderContext(
            cryptoContext,
            PrivateKeyRing(cryptoContext, listOf(vaultKey)),
            PublicKeyRing(listOf(vaultKey.publicKey(cryptoContext)))
        )

        val decryptedContents = keyHolderContext.use {
            val decoded = it.getBase64Decoded(shareResponse.content!!)
            val packets = it.getEncryptedPackets(it.getArmored(decoded))
            it.decryptData(packets.dataPacket(), packets.keyPacket())
        }

        return VaultV1.Vault.parseFrom(decryptedContents)
    }
}
