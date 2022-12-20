package me.proton.android.pass.data.impl.crypto

import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import me.proton.android.pass.data.impl.error.InvalidSignature
import me.proton.android.pass.data.impl.error.KeyNotFound
import me.proton.android.pass.data.impl.extensions.fromParsed
import me.proton.android.pass.data.impl.responses.ItemRevision
import me.proton.android.pass.log.PassLogger
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.verifyData
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.ShareType
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.domain.key.publicKey
import me.proton.pass.domain.key.usePrivateKey
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

interface OpenItem {
    fun open(
        response: ItemRevision,
        share: Share,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Item
}

class OpenItemImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val encryptionContextProvider: EncryptionContextProvider
) : OpenItem, BaseCryptoOperation(cryptoContext) {

    @Suppress("TooGenericExceptionThrown")
    override fun open(
        response: ItemRevision,
        share: Share,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Item {
        return when (share.shareType) {
            ShareType.Vault -> openItemWithVaultShare(
                response,
                share.id,
                verifyKeys,
                vaultKeys,
                itemKeys
            )
            else -> throw Exception("Not implemented yet")
        }
    }

    private fun openItemWithVaultShare(
        response: ItemRevision,
        shareId: ShareId,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Item {
        val (vaultKey, itemKey) = getKeys(response, vaultKeys, itemKeys)

        val (decryptedContents, decryptedUserSignature, decryptedItemSignature) =
            vaultKey.usePrivateKey(cryptoContext) {
                val decryptedContents = decryptData(getArmored(getBase64Decoded(response.content)))
                val decryptedUserSignature =
                    decryptData(getArmored(getBase64Decoded(response.userSignature)))
                val decryptedItemSignature =
                    decryptData(getArmored(getBase64Decoded(response.itemKeySignature)))
                Triple(decryptedContents, decryptedUserSignature, decryptedItemSignature)
            }

        val armoredUserSignature =
            cryptoContext.pgpCrypto.getArmored(decryptedUserSignature, PGPHeader.Signature)
        val armoredItemSignature =
            cryptoContext.pgpCrypto.getArmored(decryptedItemSignature, PGPHeader.Signature)

        val publicKeyRing = PublicKeyRing(verifyKeys)
        val isUserSignatureValid =
            publicKeyRing.verifyData(cryptoContext, decryptedContents, armoredUserSignature)
        if (!isUserSignatureValid) {
            PassLogger.i(
                TAG,
                "User signature for item not valid [shareId=${shareId.id}] [itemId=${response.itemId}]"
            )
            throw InvalidSignature("User signature for item")
        }

        val itemPublicKey = itemKey.publicKey(cryptoContext)
        val isItemSignatureValid = cryptoContext.pgpCrypto.verifyData(
            decryptedContents,
            armoredItemSignature,
            itemPublicKey.key
        )
        if (!isItemSignatureValid) {
            PassLogger.i(
                TAG,
                "Item signature with itemKey not valid [shareId=${shareId.id}]" +
                    "[itemId=${response.itemId}] [rotationId=${response.rotationId}]"
            )
            throw InvalidSignature("ItemKey signature for item")
        }

        val decoded = ItemV1.Item.parseFrom(decryptedContents)
        return encryptionContextProvider.withContext {
            Item(
                id = ItemId(response.itemId),
                revision = response.revision,
                shareId = shareId,
                title = encrypt(decoded.metadata.name),
                note = encrypt(decoded.metadata.note),
                content = encrypt(decryptedContents),
                itemType = ItemType.fromParsed(this, decoded, aliasEmail = response.aliasEmail),
                allowedPackageNames = decoded.platformSpecific.android.allowedAppsList
                    .map { it.packageName }
            )
        }
    }

    private fun getKeys(
        response: ItemRevision,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Pair<VaultKey, ItemKey> {
        val vaultKey = vaultKeys.firstOrNull { it.rotationId == response.rotationId }
        if (vaultKey == null) {
            PassLogger.i(
                TAG,
                "Could not find VaultKey [itemId=${response.itemId}] [rotationId=${response.rotationId}]"
            )
            throw KeyNotFound("Could not find VaultKey")
        }

        val itemKey = itemKeys.firstOrNull { it.rotationId == response.rotationId }
        if (itemKey == null) {
            PassLogger.i(
                TAG,
                "Could not find ItemKey [itemId=${response.itemId}] [rotationId=${response.rotationId}]"
            )
            throw KeyNotFound("Could not find ItemKey")
        }

        return vaultKey to itemKey
    }

    companion object {
        private const val TAG = "OpenItemImpl"
    }
}
