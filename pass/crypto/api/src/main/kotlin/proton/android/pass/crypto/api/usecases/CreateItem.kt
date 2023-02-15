package proton.android.pass.crypto.api.usecases

import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.EncryptionKey
import proton.pass.domain.ItemContents
import proton.pass.domain.key.ShareKey

data class EncryptedCreateItem(
    val keyRotation: Long,
    val contentFormatVersion: Int,
    val content: String,
    val itemKey: String
)

data class CreateItemPayload(
    val request: EncryptedCreateItem,
    val itemKey: EncryptionKey
)

interface CreateItem {
    fun create(
        userAddress: UserAddress,
        shareKey: ShareKey,
        itemContents: ItemContents
    ): CreateItemPayload
}
