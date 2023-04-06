package proton.android.pass.crypto.impl.usecases

import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.pass.domain.key.ItemKey
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

class UpdateItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : UpdateItem {

    override fun createRequest(
        itemKey: ItemKey,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): EncryptedUpdateItemRequest {
        val serializedItem = itemContent.toByteArray()
        val decryptedItemKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(itemKey.key))
        }

        val encryptedContents = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            encrypt(serializedItem, EncryptionTag.ItemContent)
        }

        return EncryptedUpdateItemRequest(
            keyRotation = itemKey.rotation,
            lastRevision = lastRevision,
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            content = Base64.encodeBase64String(encryptedContents.array)
        )
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}
