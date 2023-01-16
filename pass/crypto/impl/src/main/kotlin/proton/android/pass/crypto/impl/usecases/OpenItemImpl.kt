package proton.android.pass.crypto.impl.usecases

import kotlinx.datetime.Instant
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.impl.extensions.fromParsed
import proton.android.pass.log.api.PassLogger
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.verifyData
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareType
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.VaultKey
import proton.pass.domain.key.publicKey
import proton.pass.domain.key.usePrivateKey
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

class OpenItemImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val encryptionContextProvider: EncryptionContextProvider
) : OpenItem, BaseCryptoOperation(cryptoContext) {

    @Suppress("TooGenericExceptionThrown")
    override fun open(
        response: EncryptedItemRevision,
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
        response: EncryptedItemRevision,
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
            val e = InvalidSignature("User signature for item")
            PassLogger.w(
                TAG,
                e,
                "User signature for item not valid [shareId=${shareId.id}] [itemId=${response.itemId}]"
            )
            throw e
        }

        val itemPublicKey = itemKey.publicKey(cryptoContext)
        val isItemSignatureValid = cryptoContext.pgpCrypto.verifyData(
            decryptedContents,
            armoredItemSignature,
            itemPublicKey.key
        )
        if (!isItemSignatureValid) {
            val e = InvalidSignature("ItemKey signature for item")
            PassLogger.w(
                TAG,
                e,
                "Item signature with itemKey not valid [shareId=${shareId.id}]" +
                    "[itemId=${response.itemId}] [rotationId=${response.rotationId}]"
            )
            throw e
        }

        val decoded = ItemV1.Item.parseFrom(decryptedContents)
        return encryptionContextProvider.withEncryptionContext {
            Item(
                id = ItemId(response.itemId),
                revision = response.revision,
                shareId = shareId,
                title = encrypt(decoded.metadata.name),
                note = encrypt(decoded.metadata.note),
                content = encrypt(decryptedContents),
                itemType = ItemType.fromParsed(this, decoded, aliasEmail = response.aliasEmail),
                allowedPackageNames = decoded.platformSpecific.android.allowedAppsList
                    .map { it.packageName },
                modificationTime = Instant.fromEpochSeconds(response.modifyTime)
            )
        }
    }

    private fun getKeys(
        response: EncryptedItemRevision,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Pair<VaultKey, ItemKey> {
        val vaultKey = vaultKeys.firstOrNull { it.rotationId == response.rotationId }
        if (vaultKey == null) {
            val e = KeyNotFound("Could not find VaultKey")
            PassLogger.w(
                TAG,
                e,
                "Could not find VaultKey [itemId=${response.itemId}] [rotationId=${response.rotationId}]"
            )
            throw e
        }

        val itemKey = itemKeys.firstOrNull { it.rotationId == response.rotationId }
        if (itemKey == null) {
            val e = KeyNotFound("Could not find ItemKey")
            PassLogger.w(
                TAG,
                e,
                "Could not find ItemKey [itemId=${response.itemId}] [rotationId=${response.rotationId}]"
            )
            throw e
        }

        return vaultKey to itemKey
    }

    companion object {
        private const val TAG = "OpenItemImpl"
    }
}

