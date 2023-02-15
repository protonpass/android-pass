package proton.android.pass.crypto.api.usecases

import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.ShareKey

data class EncryptedItemKey(
    val key: String,
    val keyRotation: Long
)

interface OpenItemKey {
    operator fun invoke(shareKey: ShareKey, key: EncryptedItemKey): ItemKey
}
