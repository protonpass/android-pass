package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.apache.commons.codec.binary.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedItemKey
import proton.android.pass.crypto.api.usecases.OpenItemKey
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.ShareKey
import javax.inject.Inject

class OpenItemKeyImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : OpenItemKey {
    override fun invoke(shareKey: ShareKey, key: EncryptedItemKey): ItemKey {
        if (shareKey.rotation != key.keyRotation) {
            throw IllegalStateException("Received ShareKey with rotation not matching ItemKey " +
                "rotation [shareKey=${shareKey.rotation}] [itemKey=${key.keyRotation}]")
        }

        val decryptedShareKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val decodedItemKey = Base64.decodeBase64(key.key)
        val decryptedItemKey = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            decrypt(EncryptedByteArray(decodedItemKey), EncryptionTag.ItemKey)
        }

        val reencryptedItemKey = encryptionContextProvider.withEncryptionContext {
            encrypt(decryptedItemKey)
        }

        return ItemKey(
            rotation = key.keyRotation,
            key = reencryptedItemKey,
            responseKey = key.key
        )
    }
}
