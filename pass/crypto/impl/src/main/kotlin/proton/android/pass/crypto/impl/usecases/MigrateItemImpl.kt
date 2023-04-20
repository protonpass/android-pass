package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedMigrateItemBody
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.pass.domain.key.ShareKey
import javax.inject.Inject

class MigrateItemImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : MigrateItem {
    override fun migrate(
        destinationKey: ShareKey,
        encryptedItemContents: EncryptedByteArray,
        contentFormatVersion: Int
    ): EncryptedMigrateItemBody {
        val (decryptedDestinationKey, decryptedContents) =
            encryptionContextProvider.withEncryptionContext {
                EncryptionKey(decrypt(destinationKey.key)) to decrypt(encryptedItemContents)
            }

        val newItemKey = EncryptionKey.generate()
        val reencryptedContents = encryptionContextProvider.withEncryptionContext(newItemKey) {
            encrypt(decryptedContents, EncryptionTag.ItemContent)
        }

        val encryptedItemKey =
            encryptionContextProvider.withEncryptionContext(decryptedDestinationKey) {
                encrypt(newItemKey.value(), EncryptionTag.ItemKey)
            }

        return EncryptedMigrateItemBody(
            keyRotation = destinationKey.rotation,
            contentFormatVersion = contentFormatVersion,
            content = Base64.encodeBase64String(reencryptedContents.array),
            itemKey = Base64.encodeBase64String(encryptedItemKey.array)
        )

    }
}
