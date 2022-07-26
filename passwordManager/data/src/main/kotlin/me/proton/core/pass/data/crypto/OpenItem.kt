package me.proton.core.pass.data.crypto

import javax.inject.Inject
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.verifyData
import me.proton.core.pass.data.extensions.fromParsed
import me.proton.core.pass.data.responses.ItemRevision
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.ShareType
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.publicKey
import me.proton.core.pass.domain.key.usePrivateKey
import proton_key_item_v1.ItemV1

class OpenItem @Inject constructor(
    private val cryptoContext: CryptoContext
) {
    fun openItem(
        response: ItemRevision,
        share: Share,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Item {
        return when (share.shareType) {
            ShareType.Vault -> openItemWithVaultShare(response, share.id, verifyKeys, vaultKeys, itemKeys)
            else -> TODO("Not implemented yet")
        }
    }

    private fun openItemWithVaultShare(
        response: ItemRevision,
        shareId: ShareId,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Item {
        val vaultKey = requireNotNull(vaultKeys.first { it.rotationId == response.rotationId })
        val itemKey = requireNotNull(itemKeys.first { it.rotationId == response.rotationId })

        val (decryptedContents, decryptedUserSignature, decryptedItemSignature) = vaultKey.usePrivateKey(cryptoContext) {
            val decryptedContents = decryptData(getArmored(getBase64Decoded(response.content)))
            val decryptedUserSignature = decryptData(getArmored(getBase64Decoded(response.userSignature)))
            val decryptedItemSignature = decryptData(getArmored(getBase64Decoded(response.itemKeySignature)))
            Triple(decryptedContents, decryptedUserSignature, decryptedItemSignature)
        }

        val armoredUserSignature = cryptoContext.pgpCrypto.getArmored(decryptedUserSignature, PGPHeader.Signature)
        val armoredItemSignature = cryptoContext.pgpCrypto.getArmored(decryptedItemSignature, PGPHeader.Signature)

        val publicKeyRing = PublicKeyRing(verifyKeys)
        val isUserSignatureValid = publicKeyRing.verifyData(cryptoContext, decryptedContents, armoredUserSignature)
        require(isUserSignatureValid)

        val itemPublicKey = itemKey.publicKey(cryptoContext)
        val isItemSignatureValid = cryptoContext.pgpCrypto.verifyData(decryptedContents, armoredItemSignature, itemPublicKey.key)
        require(isItemSignatureValid)

        val decoded = ItemV1.Item.parseFrom(decryptedContents)
        val reencryptedContents = PlainByteArray(decryptedContents).encrypt(cryptoContext.keyStoreCrypto)
        return Item(
            id = ItemId(response.itemId),
            shareId = shareId,
            title = decoded.name.encrypt(cryptoContext.keyStoreCrypto),
            content = reencryptedContents,
            itemType = ItemType.fromParsed(cryptoContext, decoded)
        )
    }
}
