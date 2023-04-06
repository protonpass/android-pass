package proton.android.pass.crypto.impl.usecases

import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.extensions.serializeToProto
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.CreateItemPayload
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.impl.usecases.Utils.generateUuid
import proton.pass.domain.ItemContents
import proton.pass.domain.key.ShareKey
import javax.inject.Inject

class CreateItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
) : CreateItem {

    override fun create(
        shareKey: ShareKey,
        itemContents: ItemContents
    ): CreateItemPayload {
        val serializedItem = itemContents.serializeToProto(itemUuid = generateUuid()).toByteArray()
        val itemKey = EncryptionKey.generate()

        val encryptedContents = encryptionContextProvider.withEncryptionContext(itemKey) {
            encrypt(serializedItem, EncryptionTag.ItemContent)
        }

        val decryptedShareKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val encryptedItemKey = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            encrypt(itemKey.key, EncryptionTag.ItemKey)
        }

        val request = EncryptedCreateItem(
            keyRotation = shareKey.rotation,
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            content = Base64.encodeBase64String(encryptedContents.array),
            itemKey = Base64.encodeBase64String(encryptedItemKey.array)
        )
        return CreateItemPayload(
            request = request,
            itemKey = itemKey
        )
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}

