package proton.android.pass.crypto.api.context

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString

// This strings must be used as-is, do not modify them unless changes in other clients are
// also applied
private const val VAULT_CONTENT_TAG = "vaultcontent"
private const val ITEM_KEY_TAG = "itemkey"
private const val ITEM_CONTENT_TAG = "itemcontent"

enum class EncryptionTag(val value: ByteArray) {
    VaultContent(VAULT_CONTENT_TAG.encodeToByteArray()),
    ItemKey(ITEM_KEY_TAG.encodeToByteArray()),
    ItemContent(ITEM_CONTENT_TAG.encodeToByteArray());
}

interface EncryptionContext {
    fun encrypt(content: String): EncryptedString
    fun encrypt(content: ByteArray, tag: EncryptionTag? = null): EncryptedByteArray

    fun decrypt(content: EncryptedString): String
    fun decrypt(content: EncryptedByteArray, tag: EncryptionTag? = null): ByteArray
}
