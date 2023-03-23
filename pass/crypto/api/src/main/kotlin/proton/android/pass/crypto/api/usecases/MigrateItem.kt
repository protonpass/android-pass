package proton.android.pass.crypto.api.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.pass.domain.key.ShareKey

data class EncryptedMigrateItemBody(
    val keyRotation: Long,
    val contentFormatVersion: Int,
    val content: String,
    val itemKey: String
)

interface MigrateItem {
    fun migrate(
        destinationKey: ShareKey,
        encryptedItemContents: EncryptedByteArray,
        contentFormatVersion: Int
    ): EncryptedMigrateItemBody
}
