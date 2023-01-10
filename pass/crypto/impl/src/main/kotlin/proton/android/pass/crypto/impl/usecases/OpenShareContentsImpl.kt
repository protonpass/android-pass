package proton.android.pass.crypto.impl.usecases

import proton.android.pass.crypto.api.usecases.OpenShareContents
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
import javax.inject.Inject

class OpenShareContentsImpl @Inject constructor(
    val cryptoContext: CryptoContext
) : OpenShareContents {

    override fun openVaultShareContents(
        encryptedShareContent: String,
        vaultKey: PrivateKey
    ): ByteArray {
        val keyHolderContext = KeyHolderContext(
            cryptoContext,
            PrivateKeyRing(cryptoContext, listOf(vaultKey)),
            PublicKeyRing(listOf(vaultKey.publicKey(cryptoContext)))
        )

        return keyHolderContext.use {
            val decoded = it.getBase64Decoded(encryptedShareContent)
            val packets = it.getEncryptedPackets(it.getArmored(decoded))
            it.decryptData(packets.dataPacket(), packets.keyPacket())
        }
    }
}

