package proton.android.pass.crypto.impl.usecases

import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.impl.extensions.serializeToProto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import proton.pass.domain.ItemContents
import proton.pass.domain.entity.PackageName
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.VaultKey
import proton.pass.domain.key.publicKey
import proton.pass.domain.key.usePrivateKey
import javax.inject.Inject

class CreateItemImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : CreateItem, BaseCryptoOperation(cryptoContext) {

    override fun create(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        userAddress: UserAddress,
        itemContents: ItemContents,
        packageName: PackageName?
    ): EncryptedCreateItem {
        val serializedItem = itemContents.serializeToProto(packageName).toByteArray()
        val vaultKeyPublicKey = vaultKey.publicKey(cryptoContext)

        val (encryptedContents, vaultKeyPacket) = encryptContent(serializedItem, vaultKeyPublicKey)
        val sessionKey = vaultKey.usePrivateKey(cryptoContext) {
            decryptSessionKey(vaultKeyPacket)
        }

        val userSignature = userAddress.useKeys(cryptoContext) { signData(serializedItem) }
        val (vaultKeyPacketSignature, itemKeySignature) = itemKey.usePrivateKey(cryptoContext) {
            Pair(signData(vaultKeyPacket), signData(serializedItem))
        }

        val encryptedUserSignature =
            cryptoContext.pgpCrypto.encryptData(unarmor(userSignature), sessionKey)
        val encryptedItemSignature =
            cryptoContext.pgpCrypto.encryptData(unarmor(itemKeySignature), sessionKey)

        return EncryptedCreateItem(
            rotationId = vaultKey.rotationId,
            labels = emptyList(),
            vaultKeyPacket = b64(vaultKeyPacket),
            vaultKeyPacketSignature = b64(unarmor(vaultKeyPacketSignature)),
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            userSignature = b64(encryptedUserSignature),
            itemKeySignature = b64(encryptedItemSignature),
            content = b64(encryptedContents)
        )
    }

    private fun encryptContent(
        serializedItem: ByteArray,
        vaultPublicKey: PublicKey
    ): Pair<Unarmored, Unarmored> {
        val encrypted = cryptoContext.pgpCrypto.encryptData(serializedItem, vaultPublicKey.key)
        val packets = cryptoContext.pgpCrypto.getEncryptedPackets(encrypted)
        return Pair(packets.dataPacket(), packets.keyPacket())
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}

