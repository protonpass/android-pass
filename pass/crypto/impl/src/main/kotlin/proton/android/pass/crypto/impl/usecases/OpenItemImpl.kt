package proton.android.pass.crypto.impl.usecases

import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.android.pass.crypto.impl.extensions.fromParsed
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareType
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName
import proton.pass.domain.key.ShareKey
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

class OpenItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : OpenItem {

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): OpenItemOutput {
        return when (share.shareType) {
            ShareType.Vault -> openItemWithVaultShare(response, share.id, shareKeys)
            ShareType.Item -> openItemWithItemShare(response, share.id, shareKeys)
        }
    }

    private fun openItemWithVaultShare(
        response: EncryptedItemRevision,
        shareId: ShareId,
        shareKeys: List<ShareKey>
    ): OpenItemOutput {
        val shareKey = shareKeys.firstOrNull { it.rotation == response.keyRotation }
            ?: throw KeyNotFound(
                "Could not find ShareKey " +
                    "[share=${shareId.id}] [keyRotation=${response.keyRotation}]"
            )

        val itemKey = response.key
            ?: throw IllegalStateException(
                "ItemRevision should contain a key for Vault share " +
                    "[share=${shareId.id}] [itemId=${response.itemId}]"
            )
        val decodedItemKey = Base64.decodeBase64(itemKey)

        val decryptedShareKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val decryptedItemKey = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            EncryptionKey(decrypt(EncryptedByteArray(decodedItemKey), EncryptionTag.ItemKey))
        }

        val encryptedItemKey = encryptionContextProvider.withEncryptionContext { encrypt(decryptedItemKey.value()) }

        val decodedItemContents = Base64.decodeBase64(response.content)
        val decryptedContents = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            decrypt(EncryptedByteArray(decodedItemContents), EncryptionTag.ItemContent)
        }

        val decoded = ItemV1.Item.parseFrom(decryptedContents)
        return OpenItemOutput(
            item = createDomainObject(response, shareId, decoded, decryptedContents),
            itemKey = encryptedItemKey
        )
    }

    private fun openItemWithItemShare(
        response: EncryptedItemRevision,
        shareId: ShareId,
        shareKeys: List<ShareKey>
    ): OpenItemOutput {

        val shareKey = shareKeys.firstOrNull { it.rotation == response.keyRotation }
            ?: throw KeyNotFound(
                "Could not find ShareKey " +
                    "[share=${shareId.id}] [keyRotation=${response.keyRotation}]"
            )

        val decryptedShareKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val decodedItemContents = Base64.decodeBase64(response.content)
        val decryptedContents = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            decrypt(EncryptedByteArray(decodedItemContents), EncryptionTag.ItemContent)
        }

        val decoded = ItemV1.Item.parseFrom(decryptedContents)
        return OpenItemOutput(
            item = createDomainObject(response, shareId, decoded, decryptedContents),
            itemKey = null
        )
    }

    private fun createDomainObject(
        response: EncryptedItemRevision,
        shareId: ShareId,
        decoded: ItemV1.Item,
        decryptedContents: ByteArray
    ): Item = encryptionContextProvider.withEncryptionContext {
        Item(
            id = ItemId(response.itemId),
            itemUuid = decoded.metadata.itemUuid,
            revision = response.revision,
            shareId = shareId,
            title = encrypt(decoded.metadata.name),
            note = encrypt(decoded.metadata.note),
            content = encrypt(decryptedContents),
            itemType = ItemType.fromParsed(this, decoded, aliasEmail = response.aliasEmail),
            packageInfoSet = decoded.platformSpecific.android.allowedAppsList
                .map { PackageInfo(PackageName(it.packageName), AppName(it.appName)) }
                .toSet(),
            state = response.state,
            createTime = Instant.fromEpochSeconds(response.createTime),
            modificationTime = Instant.fromEpochSeconds(response.modifyTime),
            lastAutofillTime = response.lastUseTime.toOption().map(Instant::fromEpochSeconds),
        )
    }
}

